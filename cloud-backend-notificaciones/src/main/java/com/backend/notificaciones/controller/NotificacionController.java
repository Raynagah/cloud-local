package com.backend.notificaciones.controller;

import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones e historial del usuario")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Obtener notificaciones del usuario autenticado")
    @GetMapping
    public ResponseEntity<List<Notificacion>> obtenerMisNotificaciones(@AuthenticationPrincipal Jwt jwt) {
        String usuarioCorreo = jwt.getClaimAsString("preferred_username");
        if (usuarioCorreo == null)
            usuarioCorreo = jwt.getClaimAsString("email");
        if (usuarioCorreo == null)
            usuarioCorreo = jwt.getSubject();

        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuarioCorreo));
    }

    @Operation(summary = "Obtener notificaciones filtrando por correo explícito")
    @GetMapping("/usuario/{usuarioCorreo}")
    public ResponseEntity<List<Notificacion>> obtenerPorUsuario(@PathVariable String usuarioCorreo) {
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuarioCorreo));
    }

    @Operation(summary = "Marcar notificación como leída")
    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    @Operation(summary = "Eliminar notificación por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}