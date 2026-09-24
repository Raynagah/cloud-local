package com.backend.producto.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.backend.producto.config.RabbitMQConfig;
import com.backend.producto.dto.OrdenEventoDTO;
import com.backend.producto.service.ProductoService;

@Component
public class ProductoListener {

    private final ProductoService productoService;

    public ProductoListener(ProductoService productoService) {
        this.productoService = productoService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK)
    public void procesarActualizacionStock(OrdenEventoDTO evento) {
        System.out.println("Evento recibido en ms-producto para la orden ID: " + evento.getOrdenId());

        for (OrdenEventoDTO.ItemDTO item : evento.getItems()) {
            try {
                productoService.descontarStockAsincrono(item.getProductoId(), item.getCantidad());
            } catch (Exception e) {
                System.err.println("Error al actualizar stock del producto " + item.getProductoId() + ": " + e.getMessage());
            }
        }
    }
}
