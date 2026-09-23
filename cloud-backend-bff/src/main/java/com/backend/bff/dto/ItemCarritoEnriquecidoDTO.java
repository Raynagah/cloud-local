package com.backend.bff.dto;

import java.math.BigDecimal;

public record ItemCarritoEnriquecidoDTO(
    Long id,
    Long productoId,
    String nombreProducto,
    String descripcionProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}