package com.backend.bff.service.impl;

import com.backend.bff.service.OrdenesBffService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrdenesBffServiceImpl implements OrdenesBffService {

    private final RestTemplate restTemplate;
    @Value("${MS_ORDENES_URL:http://localhost:8085}")
    private String ordenesServiceUrl;

    @Value("${MS_ORDENES_URL:http://localhost:8085}")
    private String msOrdenesUrl;

    public OrdenesBffServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object realizarCheckout(String token, Object requestBody) {
        HttpHeaders headers = crearHeadersConToken(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                msOrdenesUrl + "/api/v1/ordenes",
                HttpMethod.POST,
                entity,
                Object.class);

        return response.getBody();
    }

    @Override
    public Object obtenerMisOrdenes(String token) {
        HttpHeaders headers = crearHeadersConToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                msOrdenesUrl + "/api/v1/ordenes",
                HttpMethod.GET,
                entity,
                Object.class);

        return response.getBody();
    }

    private HttpHeaders crearHeadersConToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    public Object obtenerOrdenPorId(String token, Long id) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, token);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    // Reemplaza ordenesServiceUrl por tu variable o la URL base de ms-ordenes (ej. "http://ms-ordenes")
    String url = ordenesServiceUrl + "/api/v1/ordenes/" + id;

    return restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class).getBody();
}
}