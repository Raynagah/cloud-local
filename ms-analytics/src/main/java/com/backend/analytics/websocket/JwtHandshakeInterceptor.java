package com.backend.analytics.websocket;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import com.backend.analytics.service.AdminAuthorizationService;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Valida el JWT de Azure AD durante el handshake del WebSocket. El navegador no puede enviar la cabecera
 * Authorization en un WebSocket, por eso el token viaja como parámetro de consulta (?token=...).
 */
@Slf4j
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final AdminAuthorizationService adminAuth;
    private final boolean requiereAutenticacion;

    public JwtHandshakeInterceptor(JwtDecoder jwtDecoder, AdminAuthorizationService adminAuth,
                                   @Value("${analytics.websocket.require-auth:true}") boolean requiereAutenticacion) {
        this.jwtDecoder = jwtDecoder;
        this.adminAuth = adminAuth;
        this.requiereAutenticacion = requiereAutenticacion;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!requiereAutenticacion) {
            return true;
        }
        String token = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            token = servletRequest.getServletRequest().getParameter("token");
        }
        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            jwtDecoder.decode(token);
        } catch (Exception e) {
            log.warn("Handshake WebSocket rechazado: token inválido ({})", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        // Autorización: solo administradores (rol verificado en ms-usuarios, no en el cliente)
        if (!adminAuth.esAdmin(token)) {
            log.warn("Handshake WebSocket rechazado: el usuario no es admin");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // nada que hacer
    }
}
