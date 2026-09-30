package com.backend.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCreadaEvent implements Serializable {

    @JsonProperty("ordenId")
    private Long ordenId;

    private String usuarioCorreo;
    private LocalDateTime fechaCreacion;
    private String estado;
    private BigDecimal total;
}
