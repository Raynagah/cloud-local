package com.backend.bff.controller;

import com.backend.bff.client.AnalyticsClient;
import com.backend.bff.dto.RecomendacionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff/analitica")
public class BffAnaliticaController {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BffAnaliticaController.class);

    private final AnalyticsClient analyticsClient;

    public BffAnaliticaController(AnalyticsClient analyticsClient) {
        this.analyticsClient = analyticsClient;
    }

    /** Recomendaciones de productos parecidos a los que el usuario ha consultado recientemente. */
    @GetMapping("/recomendaciones")
    public ResponseEntity<List<RecomendacionDTO>> recomendaciones(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Integer max) {
        try {
            return ResponseEntity.ok(analyticsClient.recomendaciones(productoId, max));
        } catch (Exception e) {
            LOG.warn("No se pudieron obtener recomendaciones: {}", e.getMessage());
            // Las recomendaciones son un extra: si el servicio de analítica falla, no se rompe la pantalla
            return ResponseEntity.ok(List.of());
        }
    }
}
