package com.backend.bff.dto;

import java.math.BigDecimal;

public record DetalleOrdenDTO(
    Long productoId,
    String nombreProducto,
    Integer cantidad,
    BigDecimal precioUnitario
) {}