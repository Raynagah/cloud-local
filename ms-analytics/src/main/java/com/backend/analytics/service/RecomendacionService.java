package com.backend.analytics.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.backend.analytics.dto.RecomendacionDTO;
import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.model.ProductoStats;
import com.backend.analytics.repository.AnalyticsStore;

/**
 * Recomendaciones de productos "parecidos" a los que el usuario ha consultado recientemente.
 *
 * Estrategia (simple y explicable):
 *  1. CO-VISUALIZACIÓN: por cada producto reciente del usuario ("semilla"), se suman los productos que
 *     otros usuarios vieron junto a él. Las semillas más recientes pesan más (decaimiento 0.8).
 *  2. PRECIO SIMILAR: si faltan resultados, se completa con productos de precio cercano al de la semilla.
 *  3. POPULARES: último recurso, los más vistos (arranque en frío).
 */
@Service
public class RecomendacionService {

    private static final int MAX_SEMILLAS = 5;
    private static final double DECAIMIENTO = 0.8;

    private final AnalyticsStore store;

    public RecomendacionService(AnalyticsStore store) {
        this.store = store;
    }

    public List<RecomendacionDTO> recomendar(String usuarioId, Long productoActual, int maximo) {
        // Semillas: producto actual primero, luego lo visto/agregado/favorito más recientemente
        Set<Long> semillas = new LinkedHashSet<>();
        if (productoActual != null) {
            semillas.add(productoActual);
        }
        for (EventoUsuario ev : store.ultimosEventos(usuarioId, 50)) {
            if (ev.getProductoId() != null && esSenalDeInteres(ev.getTipo())) {
                semillas.add(ev.getProductoId());
            }
            if (semillas.size() >= MAX_SEMILLAS) {
                break;
            }
        }

        Map<Long, ProductoStats> catalogo = new HashMap<>();
        for (ProductoStats p : store.listarProductos()) {
            catalogo.put(p.productoId(), p);
        }

        // 1) Co-visualización ponderada
        Map<Long, Double> puntajes = new HashMap<>();
        double peso = 1.0;
        for (Long semilla : semillas) {
            for (Map.Entry<Long, Long> par : store.coVisualizacionesDe(semilla).entrySet()) {
                // Solo se excluye el producto que se está mirando: con un catálogo pequeño, excluir
                // también todo lo visto antes dejaría la lista vacía.
                if (!par.getKey().equals(productoActual)) {
                    puntajes.merge(par.getKey(), peso * par.getValue(), Double::sum);
                }
            }
            peso *= DECAIMIENTO;
        }

        List<RecomendacionDTO> resultado = new ArrayList<>();
        Set<Long> yaIncluidos = new HashSet<>();
        if (productoActual != null) {
            yaIncluidos.add(productoActual);
        }

        puntajes.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maximo)
                .forEach(en -> {
                    ProductoStats p = catalogo.get(en.getKey());
                    resultado.add(new RecomendacionDTO(en.getKey(),
                            p != null ? p.nombre() : null, p != null ? p.precio() : null,
                            en.getValue(), "Otros usuarios que vieron productos similares también vieron este"));
                    yaIncluidos.add(en.getKey());
                });

        // 2) Precio similar al producto de referencia
        if (resultado.size() < maximo && !semillas.isEmpty()) {
            ProductoStats ref = catalogo.get(semillas.iterator().next());
            if (ref != null && ref.precio() != null && ref.precio().signum() > 0) {
                catalogo.values().stream()
                        .filter(p -> !yaIncluidos.contains(p.productoId()) && p.precio() != null)
                        .map(p -> Map.entry(p, similitudPrecio(ref.precio(), p.precio())))
                        .filter(en -> en.getValue() >= 0.5)
                        .sorted(Map.Entry.<ProductoStats, Double>comparingByValue().reversed())
                        .limit(maximo - resultado.size())
                        .forEach(en -> {
                            ProductoStats p = en.getKey();
                            resultado.add(new RecomendacionDTO(p.productoId(), p.nombre(), p.precio(),
                                    0.1 * en.getValue(), "Precio similar a lo que estás viendo"));
                            yaIncluidos.add(p.productoId());
                        });
            }
        }

        // 3) Populares (arranque en frío)
        if (resultado.size() < maximo) {
            catalogo.values().stream()
                    .filter(p -> !yaIncluidos.contains(p.productoId()))
                    .sorted(Comparator.comparingLong(ProductoStats::vistas).reversed())
                    .limit(maximo - resultado.size())
                    .forEach(p -> resultado.add(new RecomendacionDTO(p.productoId(), p.nombre(), p.precio(),
                            0.01, "Popular entre los usuarios")));
        }

        return resultado;
    }

    private boolean esSenalDeInteres(String tipo) {
        return "PRODUCTO_VISTO".equals(tipo) || "CARRITO_ITEM_AGREGADO".equals(tipo)
                || "FAVORITO_AGREGADO".equals(tipo);
    }

    /** 1.0 = mismo precio; 0.0 = muy distinto. */
    static double similitudPrecio(BigDecimal a, BigDecimal b) {
        double x = a.doubleValue();
        double y = b.doubleValue();
        double max = Math.max(x, y);
        return max == 0 ? 0 : 1.0 - Math.abs(x - y) / max;
    }
}
