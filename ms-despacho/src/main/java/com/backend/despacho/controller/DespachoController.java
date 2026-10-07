package com.backend.despacho.controller;

import com.backend.despacho.model.Despacho;
import com.backend.despacho.model.EstadoDespacho;
import com.backend.despacho.service.DespachoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;

    @GetMapping("/mis-despachos")
    public ResponseEntity<List<Despacho>> obtenerMisDespachos(@AuthenticationPrincipal Jwt jwt) {
        // Extraemos el claim 'sub' (o también podrías usar jwt.getClaimAsString("oid"))
        String usuarioId = jwt.getSubject(); 
        
        List<Despacho> despachos = despachoService.obtenerDespachosPorUsuario(usuarioId);
        return ResponseEntity.ok(despachos);
    }

    @GetMapping("/orden/{ordenId}/historial")
    public ResponseEntity<Despacho> obtenerHistorialDespacho(@PathVariable Long ordenId) {
        return ResponseEntity.ok(despachoService.obtenerHistorialPorOrden(ordenId));
    }

    @PutMapping("/orden/{ordenId}/estado")
    public ResponseEntity<Despacho> actualizarEstadoDespacho(
            @PathVariable Long ordenId,
            @RequestBody Map<String, String> payload) {
        
        EstadoDespacho nuevoEstado = EstadoDespacho.valueOf(payload.get("estado").toUpperCase());
        String observacion = payload.getOrDefault("observacion", "Estado actualizado por operador");
        
        Despacho actualizado = despachoService.actualizarEstado(ordenId, nuevoEstado, observacion);
        return ResponseEntity.ok(actualizado);
    }
}