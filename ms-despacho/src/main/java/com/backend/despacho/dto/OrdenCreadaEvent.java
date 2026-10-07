package com.backend.despacho.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCreadaEvent implements Serializable {

    @JsonProperty("orden_id")
    private Long ordenId;
    
    private String usuarioId;       // Para asociar el despacho al usuario en BD
    private String usuarioNombre;   // Útil para personalizar el correo de envío
    private String usuarioCorreo;   // Esencial para enviar las notificaciones (Java Mail Sender)
    private LocalDateTime fechaCreacion;
    private String estado;
    private BigDecimal total;
    private List<DetalleOrdenDTO> items;

    // Clase interna para mapear los items que vienen desde ms-ordenes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleOrdenDTO implements Serializable {
        private Long productoId;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        // Si hay otros campos en el DetalleOrdenDTO original de ms-ordenes, 
        // Jackson los ignorará sin problemas si no los declaramos aquí, 
        // o podemos agregarlos si ms-despacho los necesita (ej. peso, dimensiones).
    }
}