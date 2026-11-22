package com.backend.pagos.controller;

import com.backend.pagos.service.PagosServices;
import com.backend.pagos.dto.PeticionPagoDTO;
import com.backend.pagos.dto.TransaccionRespuestaDTO;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagosController {

    @Autowired 
    private PagosServices pagosServices;

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciarPago(@RequestBody PeticionPagoDTO peticion) {
        // 1. El servicio evalúa si ya pasaron los 30 minutos de forma automática
        if (pagosServices.verificarSiUsuarioEstaBloqueado(peticion.getClientId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Demasiados intentos detectados. Estás bloqueado temporalmente por 30 minutos. Por favor, espera."));
        }

        // 2. Si ya pasó el tiempo (o nunca se bloqueó), produce el evento a Kafka
        pagosServices.producirIntentoPago(peticion.getClientId(), peticion.getAmount(), peticion.getTransactionId());
        
        try {
            // Iniciar transacción en WebPay (Redirige al frontend en el callback)
            TransaccionRespuestaDTO transaccionWebpay = pagosServices.iniciarTransaccionWebpay(
                    peticion.getTransactionId(),
                    peticion.getClientId(),
                    peticion.getAmount().doubleValue(),
                    "http://localhost/desarrollo/callback"
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