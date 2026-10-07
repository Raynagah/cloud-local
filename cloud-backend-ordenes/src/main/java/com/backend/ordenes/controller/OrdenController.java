package com.backend.ordenes.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping
    public ResponseEntity<List<Orden>> obtenerOrdenesPorUsuario(@AuthenticationPrincipal Jwt jwt) {
        String usuarioCorreo = jwt.getClaimAsString("preferred_username");
        if (usuarioCorreo == null) {
            usuarioCorreo = jwt.getClaimAsString("email");
        }
        if (usuarioCorreo == null) {
            usuarioCorreo = jwt.getSubject();
        }

        List<Orden> ordenes = ordenService.obtenerOrdenesPorUsuario(usuarioCorreo);
        return ResponseEntity.ok(ordenes);
    }

    @PostMapping
    public ResponseEntity<Orden> crearOrden(
            @RequestBody OrdenRequestDTO requestDTO,
            @AuthenticationPrincipal Jwt jwt) {

        // 1. Identificador único universal del usuario (Sub/Oid de Azure) -> Se usa
        // para la BD
        String usuarioId = jwt.getSubject();

        // 2. Nombre completo legible del usuario
        String usuarioNombre = jwt.getClaimAsString("name");

        // 3. Correo electrónico legible (preferred_username o email)
        String usuarioCorreo = jwt.getClaimAsString("preferred_username");
        if (usuarioCorreo == null) {
            usuarioCorreo = jwt.getClaimAsString("email");
        }

        // Fallbacks por si algún claim opcional no viniera en el token
        if (usuarioCorreo == null) {
            usuarioCorreo = usuarioId;
        }
        if (usuarioNombre == null) {
            usuarioNombre = usuarioCorreo;
        }

        // Enviar los 3 datos al Service para la persistencia y la publicación del
        // evento en RabbitMQ
        Orden nuevaOrden = ordenService.crearOrden(requestDTO, usuarioId, usuarioNombre, usuarioCorreo);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Orden> obtenerOrdenPorId(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        Orden orden = ordenService.obtenerOrdenPorId(id);
        return ResponseEntity.ok(orden);
    }
}