package com.backend.bff.service;

import com.backend.bff.dto.ProductoDTO;
import java.util.List;

public interface ProductoBffService {
    List<ProductoDTO> listarProductosActivos();
    ProductoDTO obtenerProductoPorId(Long id);
}