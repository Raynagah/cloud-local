package com.backend.bff.client;

import com.backend.bff.dto.OrdenRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ordenes-client", url = "${microservicios.ordenes.url}/api/v1/ordenes")
public interface OrdenesClient {

    @GetMapping
    Object obtenerOrdenesPorUsuario();

    @PostMapping
    Object crearOrden(@RequestBody OrdenRequestDTO requestDTO);

    @GetMapping("/{id}")
    Object obtenerOrdenPorId(@PathVariable("id") Long id);
}