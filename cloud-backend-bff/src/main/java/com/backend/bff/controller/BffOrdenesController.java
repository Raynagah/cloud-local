package com.backend.bff.controller;

import com.backend.bff.dto.OrdenRequestDTO;
import com.backend.bff.service.OrdenesBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/ordenes")
@RequiredArgsConstructor
public class BffOrdenesController {

    private final OrdenesBffService ordenesBffService;

    @PostMapping("/checkout")
    public ResponseEntity<Object> realizarCheckout(@RequestBody OrdenRequestDTO requestDTO) {
        Object response = ordenesBffService.realizarCheckout(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Object> getOrdenes() {
        return ResponseEntity.ok(ordenesBffService.obtenerMisOrdenes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrdenPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ordenesBffService.obtenerOrdenPorId(id));
    }

    @GetMapping("/{id}/detalle-completo")
    public ResponseEntity<Object> getDetalleCompleto(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ordenesBffService.obtenerDetalleCompleto(id));
    }    
}