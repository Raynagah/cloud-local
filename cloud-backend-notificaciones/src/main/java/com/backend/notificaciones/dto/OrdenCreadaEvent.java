package com.backend.notificaciones.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCreadaEvent implements Serializable {

    @JsonProperty("ordenId")
    private Long ordenId;

    private String usuarioCorreo;
    private LocalDateTime fecha;
    private BigDecimal total;
}