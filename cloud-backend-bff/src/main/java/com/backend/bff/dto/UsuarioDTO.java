// src/main/java/com/backend/bff/dto/UsuarioDTO.java
package com.backend.bff.dto;

public record UsuarioDTO(
    Long id,
    String nombre,
    String telefono,
    String correo,
    Integer edad,
    String genero,
    String direccion,
    String ocupacion,
    String fotoUrl,
    String tipoUsuario
) {}