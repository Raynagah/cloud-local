package com.backend.pagos.component;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.backend.pagos.model.EstadoPagoCliente;
import com.backend.pagos.repository.EstadoPagoClienteRepositorio;
import com.backend.pagos.dto.IntentoPagoDTO;

@Slf4j
@Component 
public class ConsumidorComponent {

    private static final String TOPIC_NOMBRE = "payment-attempts";
    
    @Autowired 
    private EstadoPagoClienteRepositorio estadoRepositorio;

    @KafkaListener(topics = TOPIC_NOMBRE, groupId = "payment-velocity-group")
    @Transactional
    public void consumirIntentoPago(IntentoPagoDTO evento) {
        log.info("Consumidor Kafka: Procesando intento para el cliente: {}", evento.getIdCliente());
        
        LocalDateTime ahora = LocalDateTime.now();
        
        EstadoPagoCliente estado = estadoRepositorio.findById(evento.getIdCliente())
                .orElseGet(() -> {
                    EstadoPagoCliente nuevoEstado = new EstadoPagoCliente();
                    nuevoEstado.setIdCliente(evento.getIdCliente());
                    nuevoEstado.setCantidadIntentos(0);
                    nuevoEstado.setBloqueado(false);
                    return nuevoEstado;
                });

        // 1. Verificar si sigue bloqueado
        if (estado.isBloqueado() && estado.getBloqueadoHasta() != null) {
            if (ahora.isBefore(estado.getBloqueadoHasta())) {
                log.warn("Consumidor: El cliente {} intentó procesar pago pero sigue bloqueado hasta {}", evento.getIdCliente(), estado.getBloqueadoHasta());
                return; 
            } else {
                estado.setBloqueado(false);
                estado.setCantidadIntentos(0);
                estado.setBloqueadoHasta(null);
            }
        }

        // 2. Ventana de Tiempo (10 segundos)
        if (estado.getActualizadoEn() != null) {
            long segundosDesdeUltimoIntento = java.time.Duration.between(estado.getActualizadoEn(), ahora).getSeconds();
            
            if (segundosDesdeUltimoIntento > 10) {
                log.info("Cliente {}: Pasaron {} segundos desde el último intento. Ventana de 10s reseteada.", evento.getIdCliente(), segundosDesdeUltimoIntento);
                estado.setCantidadIntentos(0);
            }
        }

        // 3. Incrementar contador
        estado.setCantidadIntentos(estado.getCantidadIntentos() + 1);
        estado.setActualizadoEn(ahora);

        // 4. Regla de Fraude: 3 intentos en menos de 10 seg
        if (estado.getCantidadIntentos() >= 3) {
            estado.setBloqueado(true);
            estado.setBloqueadoHasta(ahora.plusMinutes(30));
            log.error("¡ALERTA FRAUDE DESDE KAFKA! Cliente {} realizó {} intentos en menos de 10 segundos. Bloqueado por 30 minutos.", 
                    evento.getIdCliente(), estado.getCantidadIntentos());
        }

        estadoRepositorio.save(estado);
    }
}