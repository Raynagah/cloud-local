package com.backend.despacho.listener;

import com.backend.despacho.config.RabbitMQConfig;
import com.backend.despacho.dto.OrdenCreadaEvent;
import com.backend.despacho.service.DespachoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DespachoListener {

    private final DespachoService despachoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DESPACHO)
    public void recibirEventoOrdenCreada(OrdenCreadaEvent evento) {
        log.info("Mensaje consumido desde RabbitMQ - Cola: {} | Orden ID: {}", RabbitMQConfig.QUEUE_DESPACHO, evento.getOrdenId());
        
        try {
            despachoService.procesarNuevaOrden(evento);
        } catch (Exception e) {
            log.error("Error crítico procesando la orden ID {}: {}", evento.getOrdenId(), e.getMessage(), e);
            throw e; // Lanza la excepción para que RabbitMQ pueda reencolar el mensaje o moverlo a una Dead Letter Queue (DLQ)
        }
    }
}