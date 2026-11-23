package com.backend.bff.client;

import com.backend.bff.dto.RecomendacionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "analytics-client", url = "${microservicios.analytics.url}/api/v1/analytics")
public interface AnalyticsClient {

    @GetMapping("/recomendaciones")
    List<RecomendacionDTO> recomendaciones(
            @RequestParam(value = "productoId", required = false) Long productoId,
            @RequestParam(value = "max", required = false) Integer max);
}
