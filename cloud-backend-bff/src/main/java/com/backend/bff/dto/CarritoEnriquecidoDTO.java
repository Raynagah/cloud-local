package com.backend.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CarritoEnriquecidoDTO(
    Long id,
    String usuarioId,
    LocalDateTime fechaCreacion,
    BigDecimal total,
    String estado,
    List<ItemCarritoEnriquecidoDTO> items
) {}