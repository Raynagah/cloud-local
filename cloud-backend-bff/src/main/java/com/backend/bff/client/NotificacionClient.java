package com.backend.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.backend.bff.dto.NotificacionDTO;
import java.util.List;

@FeignClient(name = "notificacion-client", url = "${microservicios.notificaciones.url}/api/v1/notificaciones")
public interface NotificacionClient {

    @GetMapping
    List<NotificacionDTO> obtenerMisNotificaciones();

    @GetMapping("/usuario/{correo}")
    List<NotificacionDTO> obtenerPorUsuario(@PathVariable("correo") String correo);

    @DeleteMapping("/{id}")
    void eliminarNotificacion(@PathVariable("id") Long id);

    @PutMapping("/{id}/leer")
    NotificacionDTO marcarComoLeida(@PathVariable("id") Long id);
}