package com.backend.pagos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "estado_pago_cliente")
@Data
public class EstadoPagoCliente {

    @Id
    @Column(name = "id_cliente", length = 150)
    private String idCliente;

    @Column(name = "cantidad_intentos", nullable = false)
    private int cantidadIntentos;

    @Column(name = "bloqueado", nullable = false)
    private boolean bloqueado;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}