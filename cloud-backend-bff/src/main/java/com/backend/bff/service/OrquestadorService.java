package com.backend.bff.service;

import com.backend.bff.dto.*;

public interface OrquestadorService {
    CarritoEnriquecidoDTO obtenerCarritoCompleto();
    CarritoEnriquecidoDTO agregarItemYEnriquecer(ItemCarritoRequestDTO requestDTO);
    CarritoEnriquecidoDTO eliminarItemYEnriquecer(Long productoId);
    void vaciarCarrito();
}