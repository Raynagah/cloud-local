package com.backend.bff.service;

import com.backend.bff.dto.OrdenRequestDTO;

public interface OrdenesBffService {
    Object realizarCheckout(OrdenRequestDTO requestDTO);
    Object obtenerMisOrdenes();
    Object obtenerOrdenPorId(Long id);
    Object obtenerDetalleCompleto(Long id);
}