package com.backend.bff.dto;

public record PeticionPagoDTO(
    String clientId,
    Long amount,
    String transactionId,
    String tarjeta
) {}
