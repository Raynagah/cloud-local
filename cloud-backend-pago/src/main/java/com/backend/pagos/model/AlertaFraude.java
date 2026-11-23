package com.backend.pagos.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Registro persistente de cada bloqueo preventivo ejecutado por la regla antifraude.
 */
@Entity
@Table(name = "alerta_fraude")
@Data
public class AlertaFraude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clave evaluada: "tarjeta:<hash>" cuando se informa tarjeta, o "cliente:<id>" como respaldo. */
    @Column(name = "clave_evaluada", nullable = false, length = 200)
    private String claveEvaluada;

    @Column(name = "cliente_id", length = 150)
    private String clienteId;

    @Column(name = "intentos_en_ventana", nullable = false)
    private int intentosEnVentana;

    @Column(name = "ventana_segundos", nullable = false)
    private int ventanaSegundos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "detectado_en", nullable = false)
    private LocalDateTime detectadoEn;

    @Column(name = "detalle", length = 500)
    private String detalle;
}
