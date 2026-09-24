package com.backend.ordenes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenEventoDTO {
    private Long ordenId;
    private String usuarioCorreo;
    private LocalDateTime fecha;
    private BigDecimal total;
    private List<DetalleOrdenDTO> items;
}