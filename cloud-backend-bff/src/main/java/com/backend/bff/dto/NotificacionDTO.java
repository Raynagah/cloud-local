package com.backend.bff.dto;

import java.time.LocalDateTime;

public record NotificacionDTO(
    Long id,
    Long ordenId,
    String usuarioCorreo,
    String titulo,
    String mensaje,
    boolean leido,
    LocalDateTime fechaCreacion
) {}