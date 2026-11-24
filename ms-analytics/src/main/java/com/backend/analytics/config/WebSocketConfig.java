package com.backend.analytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.backend.analytics.websocket.JwtHandshakeInterceptor;
import com.backend.analytics.websocket.MetricasWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MetricasWebSocketHandler handler;
    private final JwtHandshakeInterceptor interceptor;
    private final String[] origenesPermitidos;

    public WebSocketConfig(MetricasWebSocketHandler handler, JwtHandshakeInterceptor interceptor,
                           @Value("${analytics.cors.allowed-origins}") String[] origenesPermitidos) {
        this.handler = handler;
        this.interceptor = interceptor;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/metrics")
                .addInterceptors(interceptor)
                .setAllowedOriginPatterns(origenesPermitidos);
    }
}
