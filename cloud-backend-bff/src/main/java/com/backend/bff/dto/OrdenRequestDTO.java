package com.backend.bff.dto;

import java.util.List;

public record OrdenRequestDTO(
    List<DetalleOrdenDTO> items
) {}