package com.backend.bff.service.impl;

import com.backend.bff.client.ProductoClient;
import com.backend.bff.dto.ProductoDTO;
import com.backend.bff.service.ProductoBffService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoBffServiceImpl implements ProductoBffService {

    private final ProductoClient productoClient;

    public ProductoBffServiceImpl(ProductoClient productoClient) {
        this.productoClient = productoClient;
    }

    @Override
    public List<ProductoDTO> listarProductosActivos() {
        return productoClient.listarProductosActivos();
    }

    @Override
    public ProductoDTO obtenerProductoPorId(Long id) {
        return productoClient.obtenerProductoPorId(id);
    }
}