package com.backend.notificaciones.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.backend.notificaciones.config.RabbitMQConfig;
import com.backend.notificaciones.dto.OrdenCreadaEvent;
import com.backend.notificaciones.dto.UsuarioActualizadoEvent;
import com.backend.notificaciones.dto.UsuarioRegistradoEvent;
import com.backend.notificaciones.service.NotificacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionEventListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = "${app.rabbitmq.queue.notificaciones:q.enviar-notificacion}")
    public void manejarOrdenCreada(OrdenCreadaEvent event) {
        log.info("Evento OrdenCreada recibido en ms-notificaciones para la orden ID: {}", event.getOrdenId());
        if (event.getUsuarioCorreo() != null && !event.getUsuarioCorreo().isBlank()) {
            notificacionService.crearNotificacionDesdeOrden(event);
        } else {
            log.warn("El evento para la orden ID {} no posee usuarioCorreo válido.", event.getOrdenId());
        }
    }

    // Listener: Registro de Usuario
    @RabbitListener(queues = RabbitMQConfig.QUEUE_USUARIO_REGISTRADO)
    public void manejarUsuarioRegistrado(UsuarioRegistradoEvent event) {
        log.info("Evento UsuarioRegistrado recibido en ms-notificaciones para: {}", event.correo());
        if (event.correo() != null && !event.correo().isBlank()) {
            notificacionService.crearNotificacionBienvenida(event);
        } else {
            log.warn("El evento de usuario registrado no contiene un correo válido.");
        }
    }

    // Listener: Actualización de Perfil
    @RabbitListener(queues = RabbitMQConfig.QUEUE_USUARIO_ACTUALIZADO)
    public void manejarUsuarioActualizado(UsuarioActualizadoEvent event) {
        log.info("Evento UsuarioActualizado recibido en ms-notificaciones para: {}", event.correo());
        if (event.correo() != null && !event.correo().isBlank()) {
            notificacionService.crearNotificacionPerfilActualizado(event);
        } else {
            log.warn("El evento de usuario actualizado no contiene un correo válido.");
        }
    }
}