package com.backend.despacho.service;

import com.backend.despacho.dto.OrdenCreadaEvent;
import com.backend.despacho.model.Despacho;
import com.backend.despacho.model.DespachoHistorial;
import com.backend.despacho.model.EstadoDespacho;
import com.backend.despacho.repository.DespachoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DespachoService {

    private final DespachoRepository despachoRepository;

    @Transactional
    public void procesarNuevaOrden(OrdenCreadaEvent evento) {
        log.info("Iniciando procesamiento de despacho para Orden ID: {}", evento.getOrdenId());

        if (despachoRepository.findByOrdenId(evento.getOrdenId()).isPresent()) {
            log.warn("El despacho para la orden ID {} ya existe.", evento.getOrdenId());
            return;
        }

        Despacho despacho = Despacho.builder()
                .ordenId(evento.getOrdenId())
                .usuarioId(evento.getUsuarioId())
                .usuarioCorreo(evento.getUsuarioCorreo())
                .estadoActual(EstadoDespacho.EN_PREPARACION)
                .build();

        DespachoHistorial historial = DespachoHistorial.builder()
                .estado(EstadoDespacho.EN_PREPARACION)
                .observacion("Orden validada. El paquete está siendo empaquetado en bodega.")
                .build();

        despacho.addHistorial(historial);
        despachoRepository.save(despacho);

        log.info("Despacho registrado exitosamente para Orden ID: {}", evento.getOrdenId());
    }

    @Transactional(readOnly = true)
    public List<Despacho> obtenerDespachosPorUsuario(String usuarioId) {
        return despachoRepository.findAllByUsuarioIdOrderByFechaActualizacionDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public Despacho obtenerHistorialPorOrden(Long ordenId) {
        return despachoRepository.findByOrdenId(ordenId)
                .orElseThrow(() -> new RuntimeException("Despacho no encontrado para la orden: " + ordenId));
    }

    @Transactional
    public Despacho actualizarEstado(Long ordenId, EstadoDespacho nuevoEstado, String observacion) {
        Despacho despacho = despachoRepository.findByOrdenId(ordenId)
                .orElseThrow(() -> new RuntimeException("Despacho no encontrado para la orden: " + ordenId));

        despacho.setEstadoActual(nuevoEstado);

        DespachoHistorial historial = DespachoHistorial.builder()
                .estado(nuevoEstado)
                .observacion(observacion)
                .build();

        despacho.addHistorial(historial);
        return despachoRepository.save(despacho);
    }
}