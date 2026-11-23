package com.backend.analytics.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.backend.analytics.service.MetricasService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket /ws/metrics: envía a todos los dashboards conectados una foto de las métricas
 * cada {analytics.websocket.interval-ms} milisegundos (y una inmediata al conectarse).
 */
@Slf4j
@Component
public class MetricasWebSocketHandler extends TextWebSocketHandler {

    private final MetricasService metricas;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<WebSocketSession> sesiones = ConcurrentHashMap.newKeySet();

    public MetricasWebSocketHandler(MetricasService metricas) {
        this.metricas = metricas;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // El decorador serializa los envíos concurrentes (WebSocketSession no es thread-safe)
        WebSocketSession segura = new ConcurrentWebSocketSessionDecorator(session, 5000, 256 * 1024);
        sesiones.add(segura);
        enviar(segura, construirMensaje());
        log.info("Dashboard conectado ({} sesiones activas)", sesiones.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.removeIf(s -> s.getId().equals(session.getId()));
        log.info("Dashboard desconectado ({} sesiones activas)", sesiones.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sesiones.removeIf(s -> s.getId().equals(session.getId()));
    }

    @Scheduled(fixedRateString = "${analytics.websocket.interval-ms:2000}")
    public void difundirMetricas() {
        if (sesiones.isEmpty()) {
            return;
        }
        TextMessage mensaje = construirMensaje();
        for (WebSocketSession s : sesiones) {
            enviar(s, mensaje);
        }
    }

    private TextMessage construirMensaje() {
        try {
            return new TextMessage(mapper.writeValueAsString(metricas.snapshot()));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo serializar el snapshot de métricas", e);
        }
    }

    private void enviar(WebSocketSession s, TextMessage mensaje) {
        try {
            if (s.isOpen()) {
                s.sendMessage(mensaje);
            }
        } catch (Exception e) {
            log.debug("Sesión {} descartada: {}", s.getId(), e.getMessage());
            sesiones.remove(s);
        }
    }
}
