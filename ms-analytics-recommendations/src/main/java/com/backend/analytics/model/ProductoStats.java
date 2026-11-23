package com.backend.analytics.model;

import java.math.BigDecimal;

/** Contadores acumulados por producto (persistidos en DynamoDB). */
public record ProductoStats(Long productoId, String nombre, BigDecimal precio,
                            long vistas, long agregados, long quitados, long favoritos) {
}
