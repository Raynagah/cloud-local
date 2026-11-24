package com.backend.bff.controller;

import com.backend.bff.client.PagosClient;
import com.backend.bff.dto.PeticionPagoDTO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Único punto de entrada del frontend para los pagos: valida el JWT (SecurityConfig) y delega en ms-pagos.
 * Si ms-pagos responde con error (p. ej. 429 por la regla antifraude o 400 por pago rechazado),
 * se propaga el mismo código y cuerpo al frontend.
 */
@RestController
@RequestMapping("/api/v1/bff/pagos")
public class BffPagosController {

    private static final Logger LOG = LoggerFactory.getLogger(BffPagosController.class);

    private final PagosClient pagosClient;

    public BffPagosController(PagosClient pagosClient) {
        this.pagosClient = pagosClient;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciar(@RequestBody PeticionPagoDTO peticion) {
        try {
            return ResponseEntity.ok(pagosClient.iniciar(peticion));
        } catch (FeignException e) {
            return propagar(e);
        }
    }

    @PostMapping("/commit")
    public ResponseEntity<?> commit(@RequestParam("token_ws") String tokenWs) {
        try {
            return ResponseEntity.ok(pagosClient.commit(tokenWs));
        } catch (FeignException e) {
            return propagar(e);
        }
    }

    @GetMapping("/alertas")
    public ResponseEntity<?> alertas() {
        try {
            return ResponseEntity.ok(pagosClient.alertas());
        } catch (FeignException e) {
            return propagar(e);
        }
    }

    private ResponseEntity<String> propagar(FeignException e) {
        LOG.warn("ms-pagos respondió con error {}: {}", e.status(), e.getMessage());
        // status() es -1 cuando ni siquiera hubo respuesta (ms-pagos caído)
        HttpStatus estado = e.status() > 0 ? HttpStatus.resolve(e.status()) : HttpStatus.BAD_GATEWAY;
        if (estado == null) {
            estado = HttpStatus.BAD_GATEWAY;
        }
        String cuerpo = e.contentUTF8();
        if (cuerpo == null || cuerpo.isBlank()) {
            cuerpo = "{\"message\":\"No se pudo procesar el pago\"}";
        }
        return ResponseEntity.status(estado).contentType(MediaType.APPLICATION_JSON).body(cuerpo);
    }
}
