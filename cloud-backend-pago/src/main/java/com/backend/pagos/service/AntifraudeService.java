package com.backend.pagos.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.pagos.dto.EvaluacionFraude;
import com.backend.pagos.model.AlertaFraude;
import com.backend.pagos.model.EstadoPagoCliente;
import com.backend.pagos.repository.AlertaFraudeRepositorio;
import com.backend.pagos.repository.EstadoPagoClienteRepositorio;

import lombok.extern.slf4j.Slf4j;

/**
 * Regla antifraude: si una misma tarjeta se usa MÁS de 3 veces dentro de una ventana
 * deslizante de 10 segundos, el intento se bloquea de forma preventiva, se persiste una
 * alerta y se publica un evento en Kafka (topic fraud-alerts).
 *
 * La evaluación es síncrona (antes de llamar a Webpay) para que el bloqueo sea realmente
 * preventivo: no depende de que un consumidor asíncrono alcance a procesar el intento.
 */
@Slf4j
@Service
public class AntifraudeService {

    public static final int MAX_USOS_PERMITIDOS = 3;
    public static final int VENTANA_SEGUNDOS = 10;
    public static final int BLOQUEO_MINUTOS = 30;

    private final EstadoPagoClienteRepositorio estadoRepositorio;
    private final AlertaFraudeRepositorio alertaRepositorio;
    private final FraudeEventPublisher publisher;
    private final Clock clock;

    /** Instantes de los últimos intentos por clave (solo lo necesario para la ventana de 10 s). */
    private final Map<String, Deque<Instant>> ventanas = new HashMap<>();

    @Autowired
    public AntifraudeService(EstadoPagoClienteRepositorio estadoRepositorio,
                             AlertaFraudeRepositorio alertaRepositorio,
                             FraudeEventPublisher publisher) {
        this(estadoRepositorio, alertaRepositorio, publisher, Clock.systemDefaultZone());
    }

    AntifraudeService(EstadoPagoClienteRepositorio estadoRepositorio,
                      AlertaFraudeRepositorio alertaRepositorio,
                      FraudeEventPublisher publisher,
                      Clock clock) {
        this.estadoRepositorio = estadoRepositorio;
        this.alertaRepositorio = alertaRepositorio;
        this.publisher = publisher;
        this.clock = clock;
    }

    /** Construye la clave a evaluar: la tarjeta si se informa; si no, el cliente. */
    public static String construirClave(String tarjeta, String clienteId) {
        if (tarjeta != null && !tarjeta.isBlank()) {
            return "tarjeta:" + tarjeta.trim();
        }
        return "cliente:" + clienteId;
    }

    /**
     * Registra un intento de pago y decide si se permite.
     * Es synchronized: la ventana deslizante es estado compartido entre peticiones concurrentes.
     */
    public synchronized EvaluacionFraude evaluarIntento(String clave, String clienteId) {
        Instant ahora = clock.instant();
        LocalDateTime ahoraLocal = aLocal(ahora);

        // 1) ¿Ya hay un bloqueo vigente?
        EstadoPagoCliente estado = estadoRepositorio.findById(clave).orElse(null);
        if (estado != null && estado.isBloqueado() && estado.getBloqueadoHasta() != null) {
            if (ahoraLocal.isBefore(estado.getBloqueadoHasta())) {
                return new EvaluacionFraude(true, false, estado.getBloqueadoHasta(), mensajeBloqueo());
            }
            // El bloqueo expiró: se libera automáticamente.
            estado.setBloqueado(false);
            estado.setBloqueadoHasta(null);
            estado.setCantidadIntentos(0);
            estadoRepositorio.save(estado);
            ventanas.remove(clave);
        }

        // 2) Ventana deslizante de 10 s: descartar intentos antiguos y sumar el actual
        Deque<Instant> ventana = ventanas.computeIfAbsent(clave, k -> new ArrayDeque<>());
        Instant limite = ahora.minusSeconds(VENTANA_SEGUNDOS);
        while (!ventana.isEmpty() && ventana.peekFirst().isBefore(limite)) {
            ventana.pollFirst();
        }
        ventana.addLast(ahora);

        // 3) Más de 3 usos en la ventana => bloqueo preventivo
        if (ventana.size() > MAX_USOS_PERMITIDOS) {
            int intentos = ventana.size();
            LocalDateTime hasta = ahoraLocal.plusMinutes(BLOQUEO_MINUTOS);

            if (estado == null) {
                estado = new EstadoPagoCliente();
                estado.setIdCliente(clave);
            }
            estado.setBloqueado(true);
            estado.setBloqueadoHasta(hasta);
            estado.setCantidadIntentos(intentos);
            estado.setActualizadoEn(ahoraLocal);
            estadoRepositorio.save(estado);

            AlertaFraude alerta = new AlertaFraude();
            alerta.setClaveEvaluada(clave);
            alerta.setClienteId(clienteId);
            alerta.setIntentosEnVentana(intentos);
            alerta.setVentanaSegundos(VENTANA_SEGUNDOS);
            alerta.setBloqueadoHasta(hasta);
            alerta.setDetectadoEn(ahoraLocal);
            alerta.setDetalle("La misma tarjeta se usó " + intentos + " veces en " + VENTANA_SEGUNDOS
                    + " segundos. Bloqueo preventivo por " + BLOQUEO_MINUTOS + " minutos.");
            AlertaFraude guardada = alertaRepositorio.save(alerta);

            log.error("ALERTA FRAUDE: {} realizó {} intentos en {}s. Bloqueado hasta {}.",
                    clave, intentos, VENTANA_SEGUNDOS, hasta);

            publisher.publicarAlerta(guardada);
            ventanas.remove(clave);
            return new EvaluacionFraude(true, true, hasta, mensajeBloqueo());
        }

        return EvaluacionFraude.permitido();
    }

    /** Tras un pago exitoso se limpia el historial de la clave (no debe quedar penalizada). */
    public synchronized void reiniciar(String clave) {
        ventanas.remove(clave);
        estadoRepositorio.findById(clave).ifPresent(e -> {
            e.setBloqueado(false);
            e.setBloqueadoHasta(null);
            e.setCantidadIntentos(0);
            estadoRepositorio.save(e);
        });
    }

    private LocalDateTime aLocal(Instant instante) {
        return LocalDateTime.ofInstant(instante, ZoneId.systemDefault());
    }

    private String mensajeBloqueo() {
        return "Transacción bloqueada preventivamente por actividad sospechosa: demasiados intentos con la misma "
                + "tarjeta en pocos segundos. Intenta nuevamente en " + BLOQUEO_MINUTOS + " minutos.";
    }
}
