package com.backend.notificaciones.dto;

import java.io.Serializable;

public record UsuarioActualizadoEvent(
        Long usuarioId,
        String correo,
        String nombre,
        String telefono,
        String direccion
) implements Serializable {}