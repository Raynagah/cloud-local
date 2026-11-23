package com.backend.analytics.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.service.EventoProcessor;
import com.backend.analytics.service.MetricasService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Consumidor del clúster Kafka. Los errores se capturan aquí para no reintentar indefinidamente
 * un mensaje malformado (poison message).
 */
@Slf4j
@Component
public class EventoKafkaListener {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final EventoProcessor processor;
    private final MetricasService metricas;

    public EventoKafkaListener(EventoProcessor processor, MetricasService metricas) {
        this.processor = processor;
        this.metricas = metricas;
    }

    @KafkaListener(topics = "${analytics.kafka.topic-eventos}", groupId = "${analytics.kafka.group-id}")
    public void onEvento(String mensaje) {
        try {
            EventoUsuario evento = mapper.readValue(mensaje, EventoUsuario.class);
            processor.procesar(evento);
        } catch (Exception e) {
            log.error("No se pudo procesar el evento '{}': {}", mensaje, e.getMessage());
        }
    }

    @KafkaListener(topics = "${analytics.kafka.topic-fraude}", groupId = "${analytics.kafka.group-id}")
    public void onAlertaFraude(String mensaje) {
        try {
            JsonNode n = mapper.readTree(mensaje);
            metricas.registrarAlertaFraude(
                    n.path("claveEvaluada").asText("desconocida"),
                    n.path("intentos").asInt(0),
                    n.path("timestamp").asLong(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("No se pudo procesar la alerta de fraude '{}': {}", mensaje, e.getMessage());
        }
    }
}
