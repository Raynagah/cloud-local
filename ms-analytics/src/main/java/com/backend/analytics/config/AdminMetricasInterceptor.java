package com.backend.analytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.backend.analytics.service.AdminAuthorizationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Exige rol admin (verificado en ms-usuarios) para el endpoint REST de métricas globales. */
@Configuration
public class AdminMetricasInterceptor implements WebMvcConfigurer {

    private final AdminAuthorizationService adminAuth;
    private final boolean requiereAutenticacion;

    public AdminMetricasInterceptor(AdminAuthorizationService adminAuth,
                                    @Value("${analytics.websocket.require-auth:true}") boolean requiereAutenticacion) {
        this.adminAuth = adminAuth;
        this.requiereAutenticacion = requiereAutenticacion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                if (!requiereAutenticacion || "OPTIONS".equals(request.getMethod())) {
                    return true;
                }
                String auth = request.getHeader("Authorization");
                String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7).trim() : null;
                if (adminAuth.esAdmin(token)) {
                    return true;
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Prohibido\", \"mensaje\": \"Se requiere rol admin para ver las métricas.\"}");
                return false;
            }
        }).addPathPatterns("/api/v1/analytics/metricas");
    }
}
