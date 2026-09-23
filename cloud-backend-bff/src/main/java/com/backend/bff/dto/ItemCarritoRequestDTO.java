package com.backend.bff.dto;

import java.math.BigDecimal;

public record ItemCarritoRequestDTO(
    Long productoId,
    Integer cantidad,
    BigDecimal precioUnitario
) {}