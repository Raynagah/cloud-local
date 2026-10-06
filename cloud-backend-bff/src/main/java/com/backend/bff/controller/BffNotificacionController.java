package com.backend.bff.controller;

import com.backend.bff.service.NotificacionBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/notificaciones")
@RequiredArgsConstructor
public class BffNotificacionController {

    private final NotificacionBffService notificacionBffService;

    @GetMapping
    public ResponseEntity<Object> obtenerMisNotificaciones(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(notificacionBffService.obtenerMisNotificaciones(token));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Object> marcarComoLeida(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(notificacionBffService.marcarComoLeida(id, token));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        notificacionBffService.eliminarNotificacion(id, token);
        return ResponseEntity.noContent().build();
    }
}