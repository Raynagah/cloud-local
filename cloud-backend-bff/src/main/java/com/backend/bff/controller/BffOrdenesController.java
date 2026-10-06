package com.backend.bff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/bff/ordenes")
@RequiredArgsConstructor
public class BffOrdenesController {

    private final RestTemplate restTemplate;

    @Value("${MS_ORDENES_URL:http://localhost:8085}")
    private String msOrdenesUrl;

    @PostMapping("/checkout")
    public ResponseEntity<Object> realizarCheckout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody(required = false) Object requestBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(
            msOrdenesUrl + "/api/v1/ordenes",
            HttpMethod.POST,
            entity,
            Object.class
        );
    }
}