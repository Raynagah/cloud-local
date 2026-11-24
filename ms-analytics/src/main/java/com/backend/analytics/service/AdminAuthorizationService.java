package com.backend.analytics.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Decide si el dueño de un JWT es administrador consultando a ms-usuarios (fuente de verdad del rol):
 * GET {usuarios.url}/api/usuarios/me con el mismo token. Falla cerrado: ante cualquier error => NO admin.
 * Cachea el veredicto unos segundos para no consultar en cada petición.
 */
@Slf4j
@Service
public class AdminAuthorizationService {

    private record Entrada(boolean admin, long expiraMs) { }

    private static final int MAX_CACHE = 1000;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final long ttlMs;
    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();

    public AdminAuthorizationService(@Value("${analytics.usuarios.url}") String baseUrl,
                                     @Value("${analytics.admin.cache-seconds:60}") long cacheSegundos) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.ttlMs = cacheSegundos * 1000;
    }

    public boolean esAdmin(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        long ahora = System.currentTimeMillis();
        Entrada cacheada = cache.get(token);
        if (cacheada != null && cacheada.expiraMs() > ahora) {
            return cacheada.admin();
        }
        Boolean veredicto = consultar(token);
        if (veredicto == null) {
            return false; // error transitorio: se deniega y no se cachea
        }
        if (cache.size() >= MAX_CACHE) {
            cache.entrySet().removeIf(e -> e.getValue().expiraMs() <= ahora);
            if (cache.size() >= MAX_CACHE) {
                cache.clear();
            }
        }
        cache.put(token, new Entrada(veredicto, ahora + ttlMs));
        return veredicto;
    }

    /** true/false = respuesta definitiva de ms-usuarios; null = no se pudo determinar. */
    private Boolean consultar(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/usuarios/me"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            if (status == 200) {
                JsonNode rol = mapper.readTree(resp.body()).get("tipoUsuario");
                return rol != null && !rol.isNull() && "admin".equalsIgnoreCase(rol.asText().trim());
            }
            if (status == 401 || status == 403 || status == 404) {
                return false;
            }
            log.warn("ms-usuarios respondió {} al verificar el rol", status);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("No se pudo verificar el rol con ms-usuarios: {}", e.getMessage());
            return null;
        }
    }
}
