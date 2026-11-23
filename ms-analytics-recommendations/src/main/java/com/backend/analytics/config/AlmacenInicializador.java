package com.backend.analytics.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.backend.analytics.repository.AnalyticsStore;
import com.backend.analytics.service.MetricasService;

import lombok.extern.slf4j.Slf4j;

/**
 * Al arrancar: crea las tablas de DynamoDB si faltan y rehidrata las métricas acumuladas.
 * Reintenta unos segundos porque DynamoDB Local puede tardar en estar listo en docker-compose.
 */
@Slf4j
@Component
public class AlmacenInicializador implements ApplicationRunner {

    private static final int INTENTOS = 10;
    private static final long ESPERA_MS = 3000;

    private final AnalyticsStore store;
    private final MetricasService metricas;

    public AlmacenInicializador(AnalyticsStore store, MetricasService metricas) {
        this.store = store;
        this.metricas = metricas;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        for (int i = 1; i <= INTENTOS; i++) {
            try {
                store.asegurarTablas();
                metricas.cargarDesdeAlmacen(store.listarProductos());
                log.info("DynamoDB listo. Métricas acumuladas cargadas.");
                return;
            } catch (Exception e) {
                log.warn("DynamoDB no disponible (intento {}/{}): {}", i, INTENTOS, e.getMessage());
                Thread.sleep(ESPERA_MS);
            }
        }
        log.error("No se pudo inicializar DynamoDB; el servicio sigue activo pero sin persistencia.");
    }
}
