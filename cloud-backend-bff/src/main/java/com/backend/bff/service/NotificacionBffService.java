package com.backend.bff.service;

import com.backend.bff.dto.NotificacionDTO;
import java.util.List;

public interface NotificacionBffService {
    List<NotificacionDTO> obtenerMisNotificaciones();
    NotificacionDTO marcarComoLeida(Long id);
    void eliminarNotificacion(Long id);
}