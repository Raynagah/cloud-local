package com.backend.bff.client;

import com.backend.bff.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "producto-client", url = "${microservicios.producto.url}/api/v1/productos")
public interface ProductoClient {

    @GetMapping
    List<ProductoDTO> listarProductosActivos();

    @GetMapping("/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);
}