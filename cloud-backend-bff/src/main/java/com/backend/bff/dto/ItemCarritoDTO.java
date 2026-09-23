package com.backend.bff.dto;

import java.math.BigDecimal;

public record ItemCarritoDTO(
    Long id,
    Long productoId,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}