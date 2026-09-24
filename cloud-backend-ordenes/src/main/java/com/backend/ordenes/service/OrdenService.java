package com.backend.ordenes.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.ordenes.config.RabbitMQConfig;
import com.backend.ordenes.dto.DetalleOrdenDTO;
import com.backend.ordenes.dto.OrdenEventoDTO;
import com.backend.ordenes.dto.OrdenRequestDTO;
import com.backend.ordenes.model.DetalleOrden;
import com.backend.ordenes.model.Orden;
import com.backend.ordenes.repository.OrdenRepository;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrdenService(OrdenRepository ordenRepository, RabbitTemplate rabbitTemplate) {
        this.ordenRepository = ordenRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Orden crearOrden(OrdenRequestDTO requestDTO, String usuarioEmail) {
        // 1. Calcular el total de la orden
        BigDecimal total = requestDTO.getItems().stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Construir la entidad Orden
        Orden orden = Orden.builder()
                .usuarioCorreo(usuarioEmail)
                .fechaCreacion(LocalDateTime.now())
                .estado("PROCESADO")
                .total(total)
                .build();

        // 3. Mapear los ítems
        for (DetalleOrdenDTO itemDto : requestDTO.getItems()) {
            DetalleOrden detalle = DetalleOrden.builder()
                    .productoId(itemDto.getProductoId())
                    .cantidad(itemDto.getCantidad())
                    .precioUnitario(itemDto.getPrecioUnitario())
                    .build();
            orden.addDetalle(detalle);
        }

        // 4. Guardar en PostgreSQL local
        Orden ordenGuardada = ordenRepository.save(orden);

        // 5. Construir y publicar evento en RabbitMQ
        OrdenEventoDTO evento = OrdenEventoDTO.builder()
                .ordenId(ordenGuardada.getId())
                .usuarioCorreo(ordenGuardada.getUsuarioCorreo())
                .fecha(ordenGuardada.getFechaCreacion())
                .total(ordenGuardada.getTotal())
                .items(requestDTO.getItems())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PEDIDOS,
                RabbitMQConfig.ROUTING_KEY_PEDIDO_CREADO,
                evento
        );

        return ordenGuardada;
    }
}