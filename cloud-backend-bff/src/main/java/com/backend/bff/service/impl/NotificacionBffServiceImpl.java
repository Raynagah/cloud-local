package com.backend.bff.service.impl;

import com.backend.bff.client.NotificacionClient;
import com.backend.bff.dto.NotificacionDTO;
import com.backend.bff.service.NotificacionBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionBffServiceImpl implements NotificacionBffService {

    private final NotificacionClient notificacionClient;

    @Override
    public List<NotificacionDTO> obtenerMisNotificaciones() {
        return notificacionClient.obtenerMisNotificaciones();
    }

    @Override
    public NotificacionDTO marcarComoLeida(Long id) {
        return notificacionClient.marcarComoLeida(id);
    }

    @Override
    public void eliminarNotificacion(Long id) {
        notificacionClient.eliminarNotificacion(id);
    }
}