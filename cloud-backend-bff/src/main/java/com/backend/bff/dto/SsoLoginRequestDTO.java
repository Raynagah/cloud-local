// src/main/java/com/backend/bff/dto/SsoLoginRequestDTO.java
package com.backend.bff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SsoLoginRequestDTO(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    String correo
) {}