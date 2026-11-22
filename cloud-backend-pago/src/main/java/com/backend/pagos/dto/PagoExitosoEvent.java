package com.backend.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoExitosoEvent {
    private String ordenId;     // Será el buyOrder de Transbank
    private String usuarioId;   // Será el sessionId de Transbank
    private BigDecimal monto;
    private String token;
}