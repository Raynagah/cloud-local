package com.backend.bff.service.impl;

import com.backend.bff.service.NotificacionBffService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificacionBffServiceImpl implements NotificacionBffService {

    private final RestTemplate restTemplate;

    @Value("${MS_NOTIFICACIONES_URL:http://localhost:8086}")
    private String msNotificacionesUrl;

    public NotificacionBffServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object obtenerMisNotificaciones(String token) {
        HttpHeaders headers = crearHeadersConToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                msNotificacionesUrl + "/api/v1/notificaciones",
                HttpMethod.GET,
                entity,
                Object.class);

        return response.getBody();
    }

    @Override
    public Object marcarComoLeida(Long id, String token) {
        HttpHeaders headers = crearHeadersConToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                msNotificacionesUrl + "/api/v1/notificaciones/" + id + "/leer",
                HttpMethod.PUT,
                entity,
                Object.class);

        return response.getBody();
    }

    @Override
    public void eliminarNotificacion(Long id, String token) {
        HttpHeaders headers = crearHeadersConToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                msNotificacionesUrl + "/api/v1/notificaciones/" + id,
                HttpMethod.DELETE,
                entity,
                Void.class);
    }

    private HttpHeaders crearHeadersConToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }
}