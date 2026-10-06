package com.backend.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.backend.bff.dto.NotificacionDTO;
import java.util.List;

@FeignClient(name = "ms-notificaciones", url = "${app.feign.ms-notificaciones.url:http://ms-notificaciones:8086}")
public interface NotificacionClient {

    @GetMapping("/api/notificaciones/usuario/{correo}")
    List<NotificacionDTO> obtenerPorUsuario(@PathVariable("correo") String correo);

    @DeleteMapping("/api/notificaciones/{id}")
    void eliminarNotificacion(@PathVariable("id") Long id);

    @PutMapping("/api/notificaciones/{id}/leer")
    NotificacionDTO marcarComoLeida(@PathVariable("id") Long id);
}