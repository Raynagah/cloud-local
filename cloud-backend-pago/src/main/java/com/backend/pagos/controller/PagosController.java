package com.backend.pagos.controller;

import com.backend.pagos.service.PagosServices;
import com.backend.pagos.service.AntifraudeService;
import com.backend.pagos.dto.EvaluacionFraude;
import com.backend.pagos.model.AlertaFraude;
import com.backend.pagos.repository.AlertaFraudeRepositorio;
import com.backend.pagos.dto.PeticionPagoDTO;
import com.backend.pagos.dto.TransaccionRespuestaDTO;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
public class PagosController {

    // URL a la que Webpay devuelve al usuario tras pagar (página del frontend)
    @org.springframework.beans.factory.annotation.Value("${pagos.webpay.return-url:http://localhost/desarrollo/callback}")
    private String returnUrl;

    @Autowired
    private PagosServices pagosServices;

    @Autowired
    private AntifraudeService antifraudeService;

    @Autowired
    private AlertaFraudeRepositorio alertaFraudeRepositorio;

    /** Últimas 50 alertas antifraude registradas (para auditoría / demo). */
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaFraude>> listarAlertas() {
        return ResponseEntity.ok(alertaFraudeRepositorio.findTop50ByOrderByDetectadoEnDesc());
    }

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciarPago(@RequestBody PeticionPagoDTO peticion) {
        // 1. REGLA ANTIFRAUDE (síncrona y preventiva): >3 usos de la misma tarjeta en 10 s => bloqueo
        String clave = AntifraudeService.construirClave(peticion.getTarjeta(), peticion.getClientId());
        EvaluacionFraude evaluacion = antifraudeService.evaluarIntento(clave, peticion.getClientId());
        if (evaluacion.bloqueado()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", evaluacion.mensaje(),
                                 "bloqueadoHasta", String.valueOf(evaluacion.bloqueadoHasta())));
        }

        // 2. Se publica el intento en Kafka (analítica del comportamiento de pago)
        pagosServices.producirIntentoPago(peticion.getClientId(), peticion.getAmount(), peticion.getTransactionId());

        try {
            // Iniciar transacción en WebPay (Redirige al frontend en el callback)
            TransaccionRespuestaDTO transaccionWebpay = pagosServices.iniciarTransaccionWebpay(
                    peticion.getTransactionId(),
                    peticion.getClientId(),
                    peticion.getAmount().doubleValue(),
                    returnUrl
            );
            
            return ResponseEntity.ok(Map.of(
                    "url", transaccionWebpay.getUrlWebpay(),
                    "token", transaccionWebpay.getToken()
            ));
            
        } catch (Exception e) {
            log.error("Error al iniciar la transacción con Webpay Plus: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo conectar con la pasarela de pagos"));
        }
    }

    @PostMapping("/commit")
    public ResponseEntity<?> confirmarPagoWebpay(@RequestParam("token_ws") String tokenWs) {
        try {
            // Instanciar usando el builder oficial para integración en la versión 6.x
            WebpayPlus.Transaction tx = WebpayPlus.Transaction.buildForIntegration(
                "597055555532", 
                IntegrationApiKeys.WEBPAY
            );
            
            // Ejecutamos el commit con el token que devuelve Transbank
            var response = tx.commit(tokenWs);
            
            // Validamos si el estado es autorizado
            if ("AUTHORIZED".equals(response.getStatus())) {
                log.info("¡Pago exitoso! Orden de compra: {}, Monto: {}", response.getBuyOrder(), response.getAmount());
                
                // Pasamos los 5 parámetros requeridos
                pagosServices.gestionarResultadoCallbackPago(
                        response.getSessionId(), 
                        true,
                        response.getBuyOrder(),
                        response.getAmount(),
                        tokenWs
                );
                return ResponseEntity.ok(response);
            } else {
                log.warn("Pago rechazado por Transbank para la orden: {}", response.getBuyOrder());
                
                // Quitamos la validación de null en getAmount() porque es un double primitivo
                pagosServices.gestionarResultadoCallbackPago(
                        response.getSessionId(), 
                        false,
                        response.getBuyOrder() != null ? response.getBuyOrder() : "N/A",
                        response.getAmount(), 
                        tokenWs
                );
                return ResponseEntity.badRequest().body(Map.of("message", "El pago fue rechazado por Transbank"));
            } 
        } catch (Exception e) {
            log.error("Error al hacer commit en Transbank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al confirmar la transacción: " + e.getMessage()));
        }
    }
}