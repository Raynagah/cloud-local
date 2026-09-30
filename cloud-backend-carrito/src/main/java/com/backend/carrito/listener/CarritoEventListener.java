package com.backend.carrito.listener;

import com.backend.carrito.config.RabbitMQConfig;
import com.backend.carrito.dto.OrdenCreadaEvent;
import com.backend.carrito.service.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarritoEventListener {

    private final CarritoService carritoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CARRITO_LIMPIAR)
    public void handleOrdenCreada(OrdenCreadaEvent event) {
        log.info("Evento OrdenCreada recibido en ms-carrito para la orden ID: {} del usuario: {}",
                event.getOrdenId(), event.getUsuarioCorreo());

        try {
            if (event.getUsuarioCorreo() != null) {
                carritoService.vaciarCarritoPorUsuario(event.getUsuarioCorreo());
            } else {
                log.warn("El evento de orden creada no contiene usuarioCorreo válido.");
            }
        } catch (Exception e) {
            log.error("Error al intentar vaciar el carrito para la orden ID {}: {}",
                    event.getOrdenId(), e.getMessage(), e);
        }
    }
}
