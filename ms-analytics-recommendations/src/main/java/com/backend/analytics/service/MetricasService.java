package com.backend.analytics.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Service;

import com.backend.analytics.dto.MetricasSnapshot;
import com.backend.analytics.dto.MetricasSnapshot.AlertaReciente;
import com.backend.analytics.dto.MetricasSnapshot.AlertasFraude;
import com.backend.analytics.dto.MetricasSnapshot.ClicsMinuto;
import com.backend.analytics.dto.MetricasSnapshot.ProductoVistas;
import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.model.ProductoStats;

/**
 * Agregador en memoria de las métricas en tiempo real. Se alimenta de cada evento de Kafka y se
 * rehidrata desde DynamoDB al arrancar (contadores por producto), por lo que sobrevive a reinicios.
 */
@Service
public class MetricasService {

    private static final long MINUTO_MS = 60_000L;
    private static final long ACTIVO_MS = 5 * MINUTO_MS;   // "usuario activo" = evento en los últimos 5 min
    private static final int MINUTOS_GRAFICO = 10;
    private static final int TOP_PRODUCTOS = 5;
    private static final int ALERTAS_RECIENTES = 5;

    private final AtomicLong totalClics = new AtomicLong();
    private final Map<String, LongAdder> porTipo = new ConcurrentHashMap<>();
    private final Map<Long, LongAdder> vistasPorProducto = new ConcurrentHashMap<>();
    private final Map<Long, String> nombres = new ConcurrentHashMap<>();
    private final Map<String, Long> ultimaActividad = new ConcurrentHashMap<>();
    private final Map<Long, LongAdder> clicsPorMinuto = new ConcurrentHashMap<>();
    private final AtomicLong alertasFraude = new AtomicLong();
    private final Deque<AlertaReciente> alertasRecientes = new java.util.concurrent.ConcurrentLinkedDeque<>();

    /** Rehidrata los contadores acumulados desde la base de datos (llamar una vez al iniciar). */
    public void cargarDesdeAlmacen(List<ProductoStats> stats) {
        for (ProductoStats p : stats) {
            if (p.nombre() != null) {
                nombres.put(p.productoId(), p.nombre());
            }
            if (p.vistas() > 0) {
                vistasPorProducto.computeIfAbsent(p.productoId(), k -> new LongAdder()).add(p.vistas());
                sumarTipo("PRODUCTO_VISTO", p.vistas());
            }
            sumarTipo("CARRITO_ITEM_AGREGADO", p.agregados());
            sumarTipo("CARRITO_ITEM_ELIMINADO", p.quitados());
            sumarTipo("FAVORITO_AGREGADO", p.favoritos());
            totalClics.addAndGet(p.vistas() + p.agregados() + p.quitados() + p.favoritos());
        }
    }

    private void sumarTipo(String tipo, long cantidad) {
        if (cantidad > 0) {
            porTipo.computeIfAbsent(tipo, k -> new LongAdder()).add(cantidad);
        }
    }

    /** Registra un evento de interacción (clic) del usuario. */
    public void registrarEvento(EventoUsuario e) {
        long ahora = e.getTimestamp() != null ? e.getTimestamp() : System.currentTimeMillis();

        totalClics.incrementAndGet();
        porTipo.computeIfAbsent(e.getTipo() != null ? e.getTipo() : "DESCONOCIDO", k -> new LongAdder()).increment();
        clicsPorMinuto.computeIfAbsent(ahora / MINUTO_MS, k -> new LongAdder()).increment();

        if (e.getUsuarioId() != null) {
            ultimaActividad.put(e.getUsuarioId(), ahora);
        }
        if (e.getProductoId() != null) {
            if (e.getProductoNombre() != null) {
                nombres.put(e.getProductoId(), e.getProductoNombre());
            }
            if ("PRODUCTO_VISTO".equals(e.getTipo())) {
                vistasPorProducto.computeIfAbsent(e.getProductoId(), k -> new LongAdder()).increment();
            }
        }
    }

    public void registrarAlertaFraude(String claveEvaluada, int intentos, long timestamp) {
        alertasFraude.incrementAndGet();
        alertasRecientes.addFirst(new AlertaReciente(claveEvaluada, intentos, timestamp));
        while (alertasRecientes.size() > ALERTAS_RECIENTES) {
            alertasRecientes.pollLast();
        }
    }

    public MetricasSnapshot snapshot() {
        return snapshot(System.currentTimeMillis());
    }

    MetricasSnapshot snapshot(long ahora) {
        // usuarios activos (y limpieza de inactivos)
        ultimaActividad.values().removeIf(ts -> ahora - ts > ACTIVO_MS);

        // eventos por tipo (copia ordenada y estable)
        Map<String, Long> tipos = new LinkedHashMap<>();
        porTipo.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(en -> tipos.put(en.getKey(), en.getValue().sum()));

        // top productos más vistos
        List<ProductoVistas> top = vistasPorProducto.entrySet().stream()
                .map(en -> new ProductoVistas(en.getKey(),
                        nombres.getOrDefault(en.getKey(), "Producto #" + en.getKey()), en.getValue().sum()))
                .sorted(Comparator.comparingLong(ProductoVistas::vistas).reversed())
                .limit(TOP_PRODUCTOS)
                .toList();

        // clics de los últimos 10 minutos (incluye minutos en cero para que el gráfico sea continuo)
        long minutoActual = ahora / MINUTO_MS;
        clicsPorMinuto.keySet().removeIf(m -> m < minutoActual - 30);
        List<ClicsMinuto> serie = new ArrayList<>();
        for (long m = minutoActual - (MINUTOS_GRAFICO - 1); m <= minutoActual; m++) {
            LongAdder a = clicsPorMinuto.get(m);
            serie.add(new ClicsMinuto(m * MINUTO_MS, a != null ? a.sum() : 0));
        }

        return new MetricasSnapshot(
                ahora,
                totalClics.get(),
                ultimaActividad.size(),
                tipos,
                top,
                serie,
                new AlertasFraude(alertasFraude.get(), new ArrayList<>(alertasRecientes)));
    }
}
