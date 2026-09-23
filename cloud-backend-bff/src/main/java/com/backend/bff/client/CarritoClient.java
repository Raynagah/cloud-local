package com.backend.bff.client;

import com.backend.bff.dto.CarritoDTO;
import com.backend.bff.dto.ItemCarritoRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "carrito-client", url = "${microservicios.carrito.url}/api/v1/carritos")
public interface CarritoClient {

    @GetMapping
    CarritoDTO obtenerCarritoActivo();

    @PostMapping("/items")
    CarritoDTO agregarItem(@RequestBody ItemCarritoRequestDTO dto);

    @DeleteMapping("/items/{productoId}")
    CarritoDTO eliminarItem(@PathVariable("productoId") Long productoId);

    @DeleteMapping
    void vaciarCarrito();
}