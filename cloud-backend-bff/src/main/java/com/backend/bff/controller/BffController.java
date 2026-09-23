package com.backend.bff.controller;

import com.backend.bff.dto.CarritoEnriquecidoDTO;
import com.backend.bff.dto.ItemCarritoRequestDTO;
import com.backend.bff.service.OrquestadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/carritos")
public class BffController {

    private final OrquestadorService orquestadorService;

    public BffController(OrquestadorService orquestadorService) {
        this.orquestadorService = orquestadorService;
    }

    @GetMapping
    public ResponseEntity<CarritoEnriquecidoDTO> obtenerCarritoCompleto() {
        return ResponseEntity.ok(orquestadorService.obtenerCarritoCompleto());
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoEnriquecidoDTO> agregarItem(@Valid @RequestBody ItemCarritoRequestDTO requestDTO) {
        CarritoEnriquecidoDTO respuesta = orquestadorService.agregarItemYEnriquecer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<CarritoEnriquecidoDTO> eliminarItem(@PathVariable Long productoId) {
        CarritoEnriquecidoDTO respuesta = orquestadorService.eliminarItemYEnriquecer(productoId);
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito() {
        orquestadorService.vaciarCarrito();
        return ResponseEntity.noContent().build();
    }
}