package com.backend.bff.dto;

import java.math.BigDecimal;

public record RecomendacionDTO(
    Long productoId,
    String nombre,
    BigDecimal precio,
    double puntaje,
    String motivo
) {}
