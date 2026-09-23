// src/main/java/com/backend/bff/client/UsuarioClient.java
package com.backend.bff.client;

import com.backend.bff.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "usuario-client", url = "${microservicios.usuario.url}")
public interface UsuarioClient {

    @PostMapping("/api/usuarios")
    UsuarioDTO crear(@RequestBody UsuarioRequestDTO dto);

    @GetMapping("/api/usuarios")
    List<UsuarioDTO> listar();

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerPorId(@PathVariable("id") Long id);

    @PostMapping("/api/usuarios/login")
    UsuarioDTO login(@RequestBody SsoLoginRequestDTO dto);

    @PutMapping("/api/usuarios/{id}")
    UsuarioDTO actualizar(@PathVariable("id") Long id, @RequestBody UsuarioUpdateDTO dto);

    @DeleteMapping("/api/usuarios/{id}")
    void eliminar(@PathVariable("id") Long id);

    // Endpoints Administrativos / Internos
    @PostMapping("/internal/admin/usuarios")
    UsuarioDTO crearPorAdmin(@RequestBody UsuarioRequestDTO dto);

    @PutMapping("/internal/admin/usuarios/{id}")
    UsuarioDTO actualizarPorAdmin(@PathVariable("id") Long id, @RequestBody UsuarioUpdateDTO dto);
}