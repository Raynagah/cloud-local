package com.backend.bff.service;

public interface OrdenesBffService {
    Object realizarCheckout(String token, Object requestBody);
    Object obtenerMisOrdenes(String token);
}