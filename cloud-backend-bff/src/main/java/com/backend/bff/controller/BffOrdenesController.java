package com.backend.bff.controller;

import com.backend.bff.service.OrdenesBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/ordenes")
@RequiredArgsConstructor
public class BffOrdenesController {

    private final OrdenesBffService ordenesBffService;

    @PostMapping("/checkout")
    public ResponseEntity<Object> realizarCheckout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody(required = false) Object requestBody) {
        Object response = ordenesBffService.realizarCheckout(token, requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Object> getOrdenes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Object response = ordenesBffService.obtenerMisOrdenes(token);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrdenPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable("id") Long id) {
        Object response = ordenesBffService.obtenerOrdenPorId(token, id);
        return ResponseEntity.ok(response);
    }
}