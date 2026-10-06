package com.backend.bff.service;

public interface NotificacionBffService {
    Object obtenerMisNotificaciones(String token);
    Object marcarComoLeida(Long id, String token);
    void eliminarNotificacion(Long id, String token);
}