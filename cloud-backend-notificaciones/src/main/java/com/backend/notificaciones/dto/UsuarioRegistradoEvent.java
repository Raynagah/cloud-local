package com.backend.notificaciones.dto;

import java.io.Serializable;

public record UsuarioRegistradoEvent(
        Long usuarioId,
        String correo,
        String nombre,
        String tipoUsuario
) implements Serializable {}