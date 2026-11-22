package com.backend.bff.service;

import java.util.Map;

public interface DespachosBffService {
    Object obtenerMisDespachos();
    Object obtenerHistorialDespacho(Long ordenId);
    Object actualizarEstadoDespacho(Long ordenId, Map<String, String> payload);
}