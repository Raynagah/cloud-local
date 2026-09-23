// src/main/java/com/backend/bff/dto/UsuarioUpdateDTO.java
package com.backend.bff.dto;

import jakarta.validation.constraints.*;

public record UsuarioUpdateDTO(
    @NotBlank @Size(min = 3, max = 50) String nombre,
    @NotNull @Min(18) @Max(100) Integer edad,
    @NotBlank String genero,
    @NotBlank String telefono,
    String fotoUrl,
    String ocupacion,
    String direccion,
    @NotBlank @Pattern(regexp = "^(admin|cliente)$") String tipoUsuario
) {}