package com.backend.bff.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.backend.bff.controller.BffAdminUsuarioController;
import com.backend.bff.controller.BffUsuarioController;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

/**
 * El BFF no decide permisos: ms-usuarios (dueño del dato "rol") autoriza con 401/403. Este handler solo
 * devuelve ese mismo estado al frontend en vez de convertirlo en un 500 genérico.
 * Acotado a los controladores de usuarios para no alterar el comportamiento del resto del BFF.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = { BffAdminUsuarioController.class, BffUsuarioController.class })
public class UsuarioErrorAdvice {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> propagar(FeignException e) {
        int status = e.status() >= 400 ? e.status() : 502; // -1 => sin conexión con ms-usuarios
        if (status >= 500) {
            log.warn("ms-usuarios respondió {} ({})", status, e.getMessage());
        }
        return ResponseEntity.status(status).body(e.contentUTF8());
    }
}
