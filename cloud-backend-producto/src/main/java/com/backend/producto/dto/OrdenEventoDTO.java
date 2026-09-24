package com.backend.producto.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenEventoDTO {

    private Long ordenId;
    private String usuarioCorreo;
    private List<ItemDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {

        private Long productoId;
        private Integer cantidad;
    }
}
