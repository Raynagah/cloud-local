package com.backend.notificaciones.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.notificaciones.dto.OrdenCreadaEvent;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public void crearNotificacionDesdeOrden(OrdenCreadaEvent evento) {
        log.info("Creando notificación para la orden ID: {} del usuario: {}", evento.getOrdenId(),
                evento.getUsuarioCorreo());

        Notificacion notificacion = Notificacion.builder()
                .ordenId(evento.getOrdenId())
                .usuarioCorreo(evento.getUsuarioCorreo())
                .titulo("¡Orden #" + evento.getOrdenId() + " Creada con Éxito!")
                .mensaje(
                        "Hola, tu orden por un total de $" + evento.getTotal() + " ha sido recibida y está en proceso.")
                .build();

        notificacionRepository.save(notificacion);
        log.info("Notificación guardada en BD exitosamente para: {}", evento.getUsuarioCorreo());
    }

    public List<Notificacion> obtenerPorUsuario(String usuarioCorreo) {
        return notificacionRepository.findByUsuarioCorreoOrderByFechaCreacionDesc(usuarioCorreo);
    }

    @Transactional
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));
        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }

    public void eliminarNotificacion(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("La notificación con ID " + id + " no existe.");
        }
        notificacionRepository.deleteById(id);
    }

    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }
}