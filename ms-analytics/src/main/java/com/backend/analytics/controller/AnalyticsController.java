package com.backend.analytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.analytics.dto.MetricasSnapshot;
import com.backend.analytics.dto.RecomendacionDTO;
import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.repository.AnalyticsStore;
import com.backend.analytics.service.MetricasService;
import com.backend.analytics.service.RecomendacionService;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final RecomendacionService recomendaciones;
    private final MetricasService metricas;
    private final AnalyticsStore store;

    public AnalyticsController(RecomendacionService recomendaciones, MetricasService metricas, AnalyticsStore store) {
        this.recomendaciones = recomendaciones;
        this.metricas = metricas;
        this.store = store;
    }

    /** Recomendaciones personalizadas para el usuario autenticado (opcionalmente según el producto que mira). */
    @GetMapping("/recomendaciones")
    public ResponseEntity<List<RecomendacionDTO>> recomendaciones(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long productoId,
            @RequestParam(defaultValue = "4") int max) {
        int limite = Math.max(1, Math.min(max, 12));
        return ResponseEntity.ok(recomendaciones.recomendar(jwt.getSubject(), productoId, limite));
    }

    /** Misma foto de métricas que viaja por WebSocket (útil como respaldo o para pruebas con curl). */
    @GetMapping("/metricas")
    public ResponseEntity<MetricasSnapshot> metricas() {
        return ResponseEntity.ok(metricas.snapshot());
    }

    /** Historial reciente de navegación del propio usuario. */
    @GetMapping("/eventos/recientes")
    public ResponseEntity<List<EventoUsuario>> eventosRecientes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "20") int limite) {
        return ResponseEntity.ok(store.ultimosEventos(jwt.getSubject(), Math.max(1, Math.min(limite, 100))));
    }
}
