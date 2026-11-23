package com.backend.pagos.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.backend.pagos.model.AlertaFraude;

import lombok.extern.slf4j.Slf4j;

/**
 * Publica las alertas de fraude en Kafka (topic fraud-alerts) para que el pipeline analítico
 * las muestre en el dashboard en tiempo real. Si Kafka no responde, el pago NO se ve afectado.
 */
@Slf4j
@Component
public class FraudeEventPublisher {

    public static final String TOPIC_ALERTAS = "fraud-alerts";

    private final KafkaTemplate<String, String> kafkaTemplateTexto;

    public FraudeEventPublisher(KafkaTemplate<String, String> kafkaTemplateTexto) {
        this.kafkaTemplateTexto = kafkaTemplateTexto;
    }

    public void publicarAlerta(AlertaFraude alerta) {
        try {
            String json = "{\"tipo\":\"ALERTA_FRAUDE\""
                    + ",\"claveEvaluada\":\"" + esc(alerta.getClaveEvaluada()) + "\""
                    + ",\"usuarioId\":\"" + esc(alerta.getClienteId()) + "\""
                    + ",\"intentos\":" + alerta.getIntentosEnVentana()
                    + ",\"ventanaSegundos\":" + alerta.getVentanaSegundos()
                    + ",\"timestamp\":" + System.currentTimeMillis() + "}";
            kafkaTemplateTexto.send(TOPIC_ALERTAS, alerta.getClaveEvaluada(), json);
        } catch (Exception e) {
            log.warn("No se pudo publicar la alerta de fraude en Kafka: {}", e.getMessage());
        }
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("[\\p{Cntrl}]", " ");
    }
}
