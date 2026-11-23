package com.backend.pagos.dto;

import lombok.Data;

@Data
public class PeticionPagoDTO {
    private String clientId;
    private Long amount;
    private String transactionId;

    /**
     * Opcional: identificador NO sensible de la tarjeta (hash o últimos 4 dígitos + BIN).
     * Si se informa, la regla antifraude cuenta los usos de esa tarjeta; si no, usa el clientId.
     * Nunca se debe enviar el número completo de la tarjeta.
     */
    private String tarjeta;
}
