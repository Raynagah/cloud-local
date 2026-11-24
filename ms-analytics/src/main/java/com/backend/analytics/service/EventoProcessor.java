package com.backend.analytics.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.repository.AnalyticsStore;

import lombok.extern.slf4j.Slf4j;

/**
 * Procesa cada evento de comportamiento: lo persiste, actualiza los contadores por producto,
 * alimenta el grafo de co-visualización (base de las recomendaciones) y las métricas en tiempo real.
 */
@Slf4j
@Service
public class EventoProcessor {

    /** Cuántos productos vistos recientemente se relacionan con el producto que se acaba de ver. */
    static final int HISTORIAL_PARA_COVISTA = 10;

    private final AnalyticsStore store;
    private final MetricasService metricas;

    public EventoProcessor(AnalyticsStore store, MetricasService metricas) {
        this.store = store;
        this.metricas = metricas;
    }

    public void procesar(EventoUsuario e) {
        if (e == null || e.getTipo() == null) {
            log.warn("Evento descartado: sin tipo");
            return;
        }
        if (e.getTimestamp() == null) {
            e.setTimestamp(System.currentTimeMillis());
        }

        // 1) Las métricas en memoria se actualizan siempre (aunque DynamoDB falle, el dashboard sigue vivo)
        metricas.registrarEvento(e);

        // 2) Persistencia (historial + contadores)
        store.guardarEvento(e);

        if (e.getProductoId() == null) {
            return;   // p. ej. CARRITO_VACIADO / COMPRA_REALIZADA
        }

        switch (e.getTipo()) {
            case "PRODUCTO_VISTO" -> {
                store.incrementarContador(e.getProductoId(), "vistas", e.getProductoNombre(), e.getPrecio());
                actualizarCoVisualizacion(e);
            }
            case "CARRITO_ITEM_AGREGADO" ->
                    store.incrementarContador(e.getProductoId(), "agregados", e.getProductoNombre(), e.getPrecio());
            case "CARRITO_ITEM_ELIMINADO" ->
                    store.incrementarContador(e.getProductoId(), "quitados", e.getProductoNombre(), e.getPrecio());
            case "FAVORITO_AGREGADO" ->
                    store.incrementarContador(e.getProductoId(), "favoritos", e.getProductoNombre(), e.getPrecio());
            default -> { /* otros tipos solo se almacenan en el historial */ }
        }
    }

    /**
     * Si el usuario vio A y antes había visto B y C, se refuerzan los pares (A,B) y (A,C) en ambos
     * sentidos. Así, "quienes vieron A también vieron B".
     */
    private void actualizarCoVisualizacion(EventoUsuario e) {
        List<EventoUsuario> recientes = store.ultimosEventos(e.getUsuarioId() != null ? e.getUsuarioId() : "anonimo", 50);
        Set<Long> previos = new LinkedHashSet<>();
        for (EventoUsuario r : recientes) {
            if ("PRODUCTO_VISTO".equals(r.getTipo()) && r.getProductoId() != null
                    && !r.getProductoId().equals(e.getProductoId())) {
                previos.add(r.getProductoId());
                if (previos.size() >= HISTORIAL_PARA_COVISTA) {
                    break;
                }
            }
        }
        for (Long otro : previos) {
            store.incrementarCoVisualizacion(e.getProductoId(), otro);
            store.incrementarCoVisualizacion(otro, e.getProductoId());
        }
    }
}
