package com.backend.bff.service.impl;

import com.backend.bff.client.DespachosClient;
import com.backend.bff.service.DespachosBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DespachosBffServiceImpl implements DespachosBffService {

    private final DespachosClient despachosClient;

    @Override
    public Object obtenerMisDespachos() {
        return despachosClient.obtenerMisDespachos();
    }

    @Override
    public Object obtenerHistorialDespacho(Long ordenId) {
        return despachosClient.obtenerHistorialDespacho(ordenId);
    }

    @Override
    public Object actualizarEstadoDespacho(Long ordenId, Map<String, String> payload) {
        return despachosClient.actualizarEstadoDespacho(ordenId, payload);
    }
}