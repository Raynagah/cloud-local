package com.backend.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class AdminAuthorizationServiceTest {

    private HttpServer server;
    private final AtomicInteger llamadas = new AtomicInteger();

    private AdminAuthorizationService conRespuesta(int status, String body) throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/usuarios/me", ex -> {
            llamadas.incrementAndGet();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(status, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.close();
        });
        server.start();
        return new AdminAuthorizationService("http://127.0.0.1:" + server.getAddress().getPort(), 60);
    }

    @AfterEach
    void parar() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void adminEsReconocido() throws Exception {
        assertTrue(conRespuesta(200, "{\"tipoUsuario\":\"admin\"}").esAdmin("tok"));
    }

    @Test
    void clienteNoEsAdmin() throws Exception {
        assertFalse(conRespuesta(200, "{\"tipoUsuario\":\"cliente\"}").esAdmin("tok"));
    }

    @Test
    void prohibidoOErrorDeServidorDeniega() throws Exception {
        assertFalse(conRespuesta(403, "{}").esAdmin("tok"));
        parar();
        assertFalse(conRespuesta(500, "{}").esAdmin("tok2"));
    }

    @Test
    void tokenVacioONuloDeniega() throws Exception {
        AdminAuthorizationService s = conRespuesta(200, "{\"tipoUsuario\":\"admin\"}");
        assertFalse(s.esAdmin(null));
        assertFalse(s.esAdmin(" "));
        assertEquals(0, llamadas.get());
    }

    @Test
    void elVeredictoSeCacheaYServidorCaidoDeniega() throws Exception {
        AdminAuthorizationService s = conRespuesta(200, "{\"tipoUsuario\":\"admin\"}");
        assertTrue(s.esAdmin("tok"));
        assertTrue(s.esAdmin("tok"));
        assertEquals(1, llamadas.get());
        parar();
        assertFalse(s.esAdmin("otro-token")); // sin servidor: falla cerrado
    }
}
