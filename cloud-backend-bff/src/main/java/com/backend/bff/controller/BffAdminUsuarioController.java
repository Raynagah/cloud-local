// src/main/java/com/backend/bff/controller/BffAdminUsuarioController.java
package com.backend.bff.controller;

import com.backend.bff.dto.*;
import com.backend.bff.service.UsuarioBffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/admin/usuarios")
public class BffAdminUsuarioController {

    private final UsuarioBffService usuarioService;

    public BffAdminUsuarioController(UsuarioBffService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crearPorAdmin(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuarioAdmin(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPorAdmin(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuarioAdmin(id, dto));
    }
}