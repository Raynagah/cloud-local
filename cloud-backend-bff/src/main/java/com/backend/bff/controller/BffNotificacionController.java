package com.backend.bff.controller;

import com.backend.bff.dto.NotificacionDTO;
import com.backend.bff.service.NotificacionBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff/notificaciones")
@RequiredArgsConstructor
public class BffNotificacionController {

    private final NotificacionBffService notificacionBffService;

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> obtenerMisNotificaciones() {
        return ResponseEntity.ok(notificacionBffService.obtenerMisNotificaciones());
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionBffService.marcarComoLeida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionBffService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}