package com.backend.ordenes.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.ordenes.config.RabbitMQConfig;
import com.backend.ordenes.dto.DetalleOrdenDTO;
import com.backend.ordenes.dto.OrdenCreadaEvent;
import com.backend.ordenes.dto.OrdenRequestDTO;
import com.backend.ordenes.model.DetalleOrden;
import com.backend.ordenes.model.Orden;
import com.backend.ordenes.repository.OrdenRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrdenService {

        private final OrdenRepository ordenRepository;
        private final RabbitTemplate rabbitTemplate;

        public OrdenService(OrdenRepository ordenRepository, RabbitTemplate rabbitTemplate) {
                this.ordenRepository = ordenRepository;
                this.rabbitTemplate = rabbitTemplate;
        }

        @Transactional(readOnly = true)
        public List<Orden> obtenerOrdenesPorUsuario(String usuarioCorreo) {
                log.info("Obteniendo historial de órdenes para: {}", usuarioCorreo);
                return ordenRepository.findAllByUsuarioCorreoOrderByFechaCreacionDesc(usuarioCorreo);
        }

        @Transactional
        public Orden crearOrden(OrdenRequestDTO requestDTO, String usuarioId, String usuarioNombre,
                        String usuarioCorreo) {

                // --- LOG DE INICIO ---
                log.info("Iniciando creación de orden para el usuario: {} ({})", usuarioNombre, usuarioCorreo);

                // 1. Calcular el total de la orden
                BigDecimal total = requestDTO.getItems().stream()
                                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 2. Construir la entidad Orden
                Orden orden = Orden.builder()
                                .usuarioCorreo(usuarioCorreo)
                                .fechaCreacion(LocalDateTime.now())
                                .estado("PROCESADO")
                                .total(total)
                                .build();

                // 3. Mapear los ítems
                for (DetalleOrdenDTO itemDto : requestDTO.getItems()) {
                        DetalleOrden detalle = DetalleOrden.builder()
                                        .productoId(itemDto.getProductoId())
                                        .nombreProducto(itemDto.getNombreProducto())
                                        .cantidad(itemDto.getCantidad())
                                        .precioUnitario(itemDto.getPrecioUnitario())
                                        .build();
                        orden.addDetalle(detalle);
                }

                // 4. Guardar en PostgreSQL local
                Orden ordenGuardada = ordenRepository.save(orden);

                // --- LOG DE GUARDADO ---
                log.info("Orden #{} guardada exitosamente en BD con total: {}", ordenGuardada.getId(),
                                ordenGuardada.getTotal());

                // 5. Construir y publicar evento completo en RabbitMQ
                OrdenCreadaEvent evento = OrdenCreadaEvent.builder()
                                .ordenId(ordenGuardada.getId())
                                .usuarioId(usuarioId)
                                .usuarioNombre(usuarioNombre)
                                .usuarioCorreo(usuarioCorreo)
                                .fechaCreacion(ordenGuardada.getFechaCreacion())
                                .estado(ordenGuardada.getEstado())
                                .total(ordenGuardada.getTotal())
                                .items(requestDTO.getItems())
                                .build();

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.EXCHANGE_PEDIDOS,
                                RabbitMQConfig.ROUTING_KEY_PEDIDO_CREADO,
                                evento);

                // --- LOG DE EVENTO RABBITMQ ---
                log.info("Evento publicado con éxito en RabbitMQ para la Orden #{}", ordenGuardada.getId());

                return ordenGuardada;
        }

        public Orden obtenerOrdenPorId(Long id) {
                return ordenRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));
        }
}