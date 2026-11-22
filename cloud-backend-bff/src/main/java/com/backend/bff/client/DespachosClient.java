package com.backend.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "despachos-client", url = "${microservicios.despachos.url}/api/v1/despachos")
public interface DespachosClient {

    @GetMapping("/mis-despachos")
    Object obtenerMisDespachos();

    @GetMapping("/orden/{ordenId}/historial")
    Object obtenerHistorialDespacho(@PathVariable("ordenId") Long ordenId);

    @PutMapping("/orden/{ordenId}/estado")
    Object actualizarEstadoDespacho(@PathVariable("ordenId") Long ordenId, @RequestBody Map<String, String> payload);
}