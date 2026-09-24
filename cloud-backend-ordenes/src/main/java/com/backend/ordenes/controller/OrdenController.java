package com.backend.ordenes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ordenes.dto.OrdenRequestDTO;
import com.backend.ordenes.model.Orden;
import com.backend.ordenes.service.OrdenService;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    public ResponseEntity<Orden> crearOrden(
            @RequestBody OrdenRequestDTO requestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        
        String usuarioCorreo = jwt.getClaimAsString("preferred_username");
        if (usuarioCorreo == null) {
            usuarioCorreo = jwt.getClaimAsString("email");
        }
        if (usuarioCorreo == null) {
            usuarioCorreo = jwt.getSubject();
        }

        Orden nuevaOrden = ordenService.crearOrden(requestDTO, usuarioCorreo);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }
}