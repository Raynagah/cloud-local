package com.backend.bff.controller;

import com.backend.bff.service.DespachosBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bff/despachos")
@RequiredArgsConstructor
public class BffDespachosController {

    private final DespachosBffService despachosBffService;

    @GetMapping("/mis-despachos")
    public ResponseEntity<Object> getMisDespachos() {
        return ResponseEntity.ok(despachosBffService.obtenerMisDespachos());
    }

    @GetMapping("/orden/{ordenId}/historial")
    public ResponseEntity<Object> getHistorialDespacho(@PathVariable Long ordenId) {
        return ResponseEntity.ok(despachosBffService.obtenerHistorialDespacho(ordenId));
    }

    @PutMapping("/orden/{ordenId}/estado")
    public ResponseEntity<Object> actualizarEstado(
            @PathVariable Long ordenId,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(despachosBffService.actualizarEstadoDespacho(ordenId, payload));
    }
}