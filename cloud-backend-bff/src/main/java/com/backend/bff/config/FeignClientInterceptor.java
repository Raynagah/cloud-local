package com.backend.bff.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // Obtenemos el contexto de seguridad de la petición actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificamos que el usuario esté autenticado mediante un token JWT
        if (authentication != null && authentication instanceof JwtAuthenticationToken jwtAuth) {
            // Extraemos el valor del token en texto plano
            String tokenValue = jwtAuth.getToken().getTokenValue();
            
            // Inyectamos el token en la cabecera de la petición saliente
            requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, tokenValue));
        }
    }
}