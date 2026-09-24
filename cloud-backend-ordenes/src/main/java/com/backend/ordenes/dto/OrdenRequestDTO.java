package com.backend.ordenes.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrdenRequestDTO {
    private List<DetalleOrdenDTO> items;
}