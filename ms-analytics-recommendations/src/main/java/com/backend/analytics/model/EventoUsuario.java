package com.backend.analytics.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de comportamiento publicado por ms-producto, ms-carrito y ms-favoritos en el topic user-events.
 * Tipos: PRODUCTO_VISTO, CARRITO_ITEM_AGREGADO, CARRITO_ITEM_ELIMINADO, CARRITO_VACIADO,
 * COMPRA_REALIZADA, FAVORITO_AGREGADO, FAVORITO_ELIMINADO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventoUsuario {
    private String eventId;
    private String tipo;
    private String usuarioId;
    private String usuarioNombre;
    private Long productoId;
    private String productoNombre;
    private BigDecimal precio;
    private Integer cantidad;
    private String origen;
    /** Epoch en milisegundos. */
    private Long timestamp;
}
