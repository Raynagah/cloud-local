package com.backend.analytics.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.model.ProductoStats;
import com.backend.analytics.repository.AnalyticsStore;

/** Almacén en memoria para probar la lógica sin DynamoDB. */
class InMemoryAnalyticsStore implements AnalyticsStore {

    final List<EventoUsuario> eventos = new ArrayList<>();
    private final Map<Long, long[]> contadores = new HashMap<>();   // vistas, agregados, quitados, favoritos
    private final Map<Long, String> nombres = new HashMap<>();
    private final Map<Long, BigDecimal> precios = new HashMap<>();
    private final Map<Long, Map<Long, Long>> coview = new HashMap<>();

    @Override public void asegurarTablas() { }

    @Override
    public void guardarEvento(EventoUsuario e) {
        eventos.add(e);
    }

    @Override
    public List<EventoUsuario> ultimosEventos(String usuarioId, int limite) {
        List<EventoUsuario> propios = new ArrayList<>();
        for (EventoUsuario e : eventos) {
            if (usuarioId.equals(e.getUsuarioId())) {
                propios.add(e);
            }
        }
        Collections.reverse(propios);   // más recientes primero
        return propios.size() > limite ? propios.subList(0, limite) : propios;
    }

    @Override
    public void incrementarContador(Long productoId, String contador, String nombre, BigDecimal precio) {
        long[] c = contadores.computeIfAbsent(productoId, k -> new long[4]);
        switch (contador) {
            case "vistas" -> c[0]++;
            case "agregados" -> c[1]++;
            case "quitados" -> c[2]++;
            case "favoritos" -> c[3]++;
            default -> throw new IllegalArgumentException(contador);
        }
        if (nombre != null) nombres.put(productoId, nombre);
        if (precio != null) precios.put(productoId, precio);
    }

    @Override
    public Optional<ProductoStats> obtenerProducto(Long id) {
        return contadores.containsKey(id) ? Optional.of(aStats(id)) : Optional.empty();
    }

    @Override
    public List<ProductoStats> listarProductos() {
        List<ProductoStats> lista = new ArrayList<>();
        for (Long id : contadores.keySet()) {
            lista.add(aStats(id));
        }
        return lista;
    }

    @Override
    public void incrementarCoVisualizacion(Long a, Long b) {
        coview.computeIfAbsent(a, k -> new LinkedHashMap<>()).merge(b, 1L, Long::sum);
    }

    @Override
    public Map<Long, Long> coVisualizacionesDe(Long id) {
        return coview.getOrDefault(id, Map.of());
    }

    private ProductoStats aStats(Long id) {
        long[] c = contadores.get(id);
        return new ProductoStats(id, nombres.get(id), precios.get(id), c[0], c[1], c[2], c[3]);
    }
}
