package com.backend.pagos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.pagos.config.RabbitMQConfig;
import com.backend.pagos.dto.IntentoPagoDTO;
import com.backend.pagos.dto.PagoExitosoEvent;
import com.backend.pagos.dto.TransaccionRespuestaDTO;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.webpay.webpayplus.WebpayPlus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagosServices {
    
    private final KafkaTemplate<String, IntentoPagoDTO> kafkaTemplate;
    private final AntifraudeService antifraudeService;
    
    // INYECTAMOS RABBITMQ
    private final RabbitTemplate rabbitTemplate;
    
    private static final String TOPIC_NOMBRE = "payment-attempts";
    
    public TransaccionRespuestaDTO iniciarTransaccionWebpay(String buyOrder, String sessionId, double amount, String returnUrl) {
        try {
            // Usamos la API key oficial de prueba de Transbank para la versión 6.x
            WebpayPlus.Transaction tx = WebpayPlus.Transaction.buildForIntegration(
                "597055555532", 
                IntegrationApiKeys.WEBPAY
            );
            
            var response = tx.create(buyOrder, sessionId, amount, returnUrl);
            
            String token = response.getToken();
            String urlWebpay = response.getUrl();
            
            log.info("Token Webpay generado con éxito: {}", token);
            return new TransaccionRespuestaDTO(token, urlWebpay);
            
        } catch (Exception e) {
            log.error("Error al conectar con Transbank: {}", e.getMessage());
            throw new RuntimeException("Error al iniciar pago en Webpay");
        }
    }
    
    // Método único para producir el intento hacia Kafka
    public void producirIntentoPago(String idCliente, Long monto, String idTransaccion) {
        try {
            IntentoPagoDTO evento = new IntentoPagoDTO(idCliente, monto, idTransaccion, LocalDateTime.now());
            kafkaTemplate.send(TOPIC_NOMBRE, idCliente, evento);
            log.info("Evento de intento enviado a Kafka para el cliente: {}", idCliente);
        } catch (Exception e) {
            // La analítica nunca debe impedir un pago
            log.warn("No se pudo publicar el intento de pago en Kafka: {}", e.getMessage());
        }
    }
    
    // Gestión del Callback (Éxito / Fallo)
    @Transactional
    public void gestionarResultadoCallbackPago(String idCliente, boolean esExitoso, String buyOrder, Double amount, String token) {
        if (esExitoso) {
            // Un pago exitoso limpia el historial de intentos del cliente.
            antifraudeService.reiniciar(AntifraudeService.construirClave(null, idCliente));
            log.info("Transacción exitosa para {}. Historial antifraude reiniciado.", idCliente);

            // DISPARAR EVENTO A RABBITMQ
            PagoExitosoEvent eventoPago = PagoExitosoEvent.builder()
                    .ordenId(buyOrder)
                    .usuarioId(idCliente)
                    .monto(BigDecimal.valueOf(amount))
                    .token(token)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PEDIDOS,
                    RabbitMQConfig.ROUTING_KEY_PAGO_EXITOSO,
                    eventoPago
            );
            log.info("Evento de Pago Exitoso enviado a RabbitMQ para la Orden: {}", buyOrder);

        } else {
            // IMPORTANTE: un rechazo NO reinicia el contador, de lo contrario se podría eludir la regla antifraude.
            log.info("Transacción rechazada/fallida para {}. Los intentos se mantienen en la ventana antifraude.", idCliente);
        }
    }
}
