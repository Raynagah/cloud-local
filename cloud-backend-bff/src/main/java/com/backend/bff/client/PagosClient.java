package com.backend.bff.client;

import com.backend.bff.dto.PeticionPagoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "pagos-client", url = "${microservicios.pagos.url}/api/pagos")
public interface PagosClient {

    @PostMapping("/iniciar")
    Map<String, Object> iniciar(@RequestBody PeticionPagoDTO peticion);

    @PostMapping("/commit")
    Map<String, Object> commit(@RequestParam("token_ws") String tokenWs);

    @GetMapping("/alertas")
    List<Map<String, Object>> alertas();
}
