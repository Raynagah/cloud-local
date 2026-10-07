package com.backend.despacho.listener;

import com.backend.despacho.config.RabbitMQConfig;
import com.backend.despacho.dto.OrdenCreadaEvent;
import com.backend.despacho.service.DespachoService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DespachoListener {

    private final DespachoService despachoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DESPACHO)
    public void recibirEventoOrdenCreada(
            OrdenCreadaEvent evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Mensaje consumido desde RabbitMQ - Cola: {} | Orden ID: {}", RabbitMQConfig.QUEUE_DESPACHO,
                evento.getOrdenId());

        try {
            // Lógica de negocio
            despachoService.procesarNuevaOrden(evento);

            // 1. Confirmación manual explícita (ACK)
            channel.basicAck(tag, false);
            log.info("ACK enviado exitosamente para el mensaje con Tag: {} | Orden ID: {}", tag, evento.getOrdenId());

        } catch (Exception e) {
            log.error("Error crítico procesando la orden ID {}: {}", evento.getOrdenId(), e.getMessage(), e);

            // 2. Manejo explícito de errores (NACK):
            // requeue = false evita bucles infinitos enviando el mensaje con fallo a una
            // DLQ (si está configurada) o descartándolo.
            channel.basicNack(tag, false, false);
        }
    }
}