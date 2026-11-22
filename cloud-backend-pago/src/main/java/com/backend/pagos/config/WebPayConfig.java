package com.backend.pagos.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebPayConfig {
    
    @PostConstruct
    public void initWebpay() {
        log.info("Configuración de Webpay lista para SDK 6.x");
    }
}