package com.backend.pagos.service;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.pagos.dto.IntentoPagoDTO;
import com.backend.pagos.model.EstadoPagoCliente;
import com.backend.pagos.repository.EstadoPagoClienteRepositorio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VelocidadPagoConsumer {

    private final EstadoPagoClienteRepositorio estadoRepositorio;
    private static final int MAX_INTENTOS_POR_MINUTO = 3;
    private static final int MINUTOS_BLOQUEO = 30;

    @Transactional
    @KafkaListener(topics = "payment-attempts", groupId = "payment-velocity-group")
    public void procesarIntentoPago(
            @Header(KafkaHeaders.RECEIVED_KEY) String idCliente,
            @Payload IntentoPagoDTO evento) {

        LocalDateTime ahora = LocalDateTime.now();
        EstadoPagoCliente estado = estadoRepositorio.findById(idCliente).orElseGet(() -> {
            EstadoPagoCliente nuevo = new EstadoPagoCliente();
            nuevo.setIdCliente(idCliente);
            nuevo.setCantidadIntentos(0);
            nuevo.setBloqueado(false);
            return nuevo;
        });

        // Si ya está bloqueado, no seguimos sumando
        if (estado.isBloqueado() && estado.getBloqueadoHasta() != null && ahora.isBefore(estado.getBloqueadoHasta())) {
            return;
        }

        // Si pasó más de 1 minuto desde el último intento, reiniciamos la ventana de velocidad
        if (estado.getActualizadoEn() != null && estado.getActualizadoEn().plusMinutes(1).isBefore(ahora)) {
            estado.setCantidadIntentos(0);
        }

        int nuevosIntentos = estado.getCantidadIntentos() + 1;
        estado.setCantidadIntentos(nuevosIntentos);
        estado.setActualizadoEn(ahora);

        if (nuevosIntentos >= MAX_INTENTOS_POR_MINUTO) {
            estado.setBloqueado(true);
            estado.setBloqueadoHasta(ahora.plusMinutes(MINUTOS_BLOQUEO));
            log.warn("ALERTA DE VELOCIDAD: Cliente {} bloqueado por {} minutos tras {} intentos.",
                    idCliente, MINUTOS_BLOQUEO, nuevosIntentos);
        } else {
            log.info("Intento #{} registrado en DB para el cliente {}", nuevosIntentos, idCliente);
        }

        estadoRepositorio.save(estado);
    }
}