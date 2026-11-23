package com.backend.pagos.dto;

import java.time.LocalDateTime;

/**
 * Resultado de evaluar un intento de pago contra la regla antifraude.
 *
 * @param bloqueado      true si el intento debe rechazarse
 * @param nuevaAlerta    true si ESTE intento fue el que disparó el bloqueo (alerta recién generada)
 * @param bloqueadoHasta fin del bloqueo (solo si bloqueado)
 * @param mensaje        texto apto para devolver al cliente
 */
public record EvaluacionFraude(boolean bloqueado, boolean nuevaAlerta, LocalDateTime bloqueadoHasta, String mensaje) {

    public static EvaluacionFraude permitido() {
        return new EvaluacionFraude(false, false, null, null);
    }
}
