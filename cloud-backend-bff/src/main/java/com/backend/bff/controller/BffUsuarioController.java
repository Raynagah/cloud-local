// src/main/java/com/backend/bff/controller/BffUsuarioController.java
package com.backend.bff.controller;

import com.backend.bff.dto.*;
import com.backend.bff.service.UsuarioBffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff/usuarios")
public class BffUsuarioController {

    private final UsuarioBffService usuarioService;

    public BffUsuarioController(UsuarioBffService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // DESPUÉS (BffUsuarioController.java)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody SsoLoginRequestDTO dto) { // Nota el <?> para permitir devolver DTO o String de error
        try {
            return ResponseEntity.ok(usuarioService.loginSSO(dto));
        } catch (feign.FeignException e) {
            // Captura el error del microservicio (ej. 401) y se lo pasa limpio al Frontend
            // e.status() tendrá el 401, y e.contentUTF8() tendrá el mensaje de tu MS
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarUsuario(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}