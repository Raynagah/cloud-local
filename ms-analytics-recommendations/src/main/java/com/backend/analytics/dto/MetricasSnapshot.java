package com.backend.analytics.dto;

import java.util.List;
import java.util.Map;

/**
 * Foto de las métricas en tiempo real que se envía por WebSocket al dashboard.
 * Métricas: totalClics, usuariosActivos, eventosPorTipo, productosMasVistos, clicsPorMinuto y alertasFraude.
 */
public record MetricasSnapshot(
        long generadoEn,
        long totalClics,
        long usuariosActivos,
        Map<String, Long> eventosPorTipo,
        List<ProductoVistas> productosMasVistos,
        List<ClicsMinuto> clicsPorMinuto,
        AlertasFraude alertasFraude) {

    public record ProductoVistas(Long productoId, String nombre, long vistas) {
    }

    /** minuto = epoch en milisegundos del inicio del minuto. */
    public record ClicsMinuto(long minuto, long clics) {
    }

    public record AlertasFraude(long total, List<AlertaReciente> recientes) {
    }

    public record AlertaReciente(String claveEvaluada, int intentos, long timestamp) {
    }
}
