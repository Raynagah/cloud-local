package com.backend.bff.service.impl;

import com.backend.bff.client.DespachosClient;
import com.backend.bff.client.OrdenesClient;
import com.backend.bff.dto.OrdenRequestDTO;
import com.backend.bff.service.OrdenesBffService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdenesBffServiceImpl implements OrdenesBffService {

    private final OrdenesClient ordenesClient;
    private final DespachosClient despachosClient;

    @Override
    public Object realizarCheckout(OrdenRequestDTO requestDTO) {
        return ordenesClient.crearOrden(requestDTO);
    }

    @Override
    public Object obtenerMisOrdenes() {
        return ordenesClient.obtenerOrdenesPorUsuario();
    }

    @Override
    public Object obtenerOrdenPorId(Long id) {
        return ordenesClient.obtenerOrdenPorId(id);
    }
    
    @Override
    public Object obtenerDetalleCompleto(Long id) {
        Map<String, Object> respuesta = new HashMap<>();
        
        // 1. Buscamos la orden
        Object orden = ordenesClient.obtenerOrdenPorId(id);
        respuesta.put("orden", orden);

        // 2. Buscamos el despacho (con Try-Catch por si el despacho aún no se crea o el MS está caído)
        try {
            Object despacho = despachosClient.obtenerHistorialDespacho(id);
            respuesta.put("despacho", despacho);
        } catch (Exception e) {
            respuesta.put("despacho", "No disponible o en proceso de creación");
        }

        return respuesta;
    }
}