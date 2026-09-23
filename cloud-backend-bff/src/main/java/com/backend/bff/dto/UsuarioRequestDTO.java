// src/main/java/com/backend/bff/dto/UsuarioRequestDTO.java
package com.backend.bff.dto;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(
    @NotBlank @Size(min = 3, max = 50) String nombre,
    @NotNull @Min(18) @Max(100) Integer edad,
    @NotBlank String genero,
    @Email @NotBlank String correo,
    @NotBlank String telefono,
    String fotoUrl,
    String ocupacion,
    @NotBlank String direccion,
    @NotBlank @Pattern(regexp = "^(admin|cliente)$") String tipoUsuario
) {}