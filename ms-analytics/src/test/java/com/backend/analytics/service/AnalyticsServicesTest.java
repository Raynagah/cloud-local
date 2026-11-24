package com.backend.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.backend.analytics.dto.MetricasSnapshot;
import com.backend.analytics.dto.RecomendacionDTO;
import com.backend.analytics.model.EventoUsuario;

class AnalyticsServicesTest {

    private InMemoryAnalyticsStore store;
    private MetricasService metricas;
    private EventoProcessor processor;
    private RecomendacionService recomendaciones;
    private long reloj;

    @BeforeEach
    void preparar() {
        store = new InMemoryAnalyticsStore();
        metricas = new MetricasService();
        processor = new EventoProcessor(store, metricas);
        recomendaciones = new RecomendacionService(store);
        reloj = 1_000_000L;
    }

    private EventoUsuario ver(String usuario, long productoId, String nombre, String precio) {
        return EventoUsuario.builder()
                .eventId("e" + (++reloj)).tipo("PRODUCTO_VISTO").usuarioId(usuario)
                .productoId(productoId).productoNombre(nombre).precio(new BigDecimal(precio))
                .timestamp(reloj).build();
    }

    @Test
    void elProcesadorActualizaContadoresYMetricas() {
        processor.procesar(ver("u1", 1, "Naruto", "24990"));
        processor.procesar(ver("u1", 1, "Naruto", "24990"));
        processor.procesar(EventoUsuario.builder().tipo("CARRITO_ITEM_AGREGADO").usuarioId("u1")
                .productoId(1L).cantidad(1).timestamp(++reloj).build());

        assertEquals(2, store.obtenerProducto(1L).orElseThrow().vistas());
        assertEquals(1, store.obtenerProducto(1L).orElseThrow().agregados());

        MetricasSnapshot snap = metricas.snapshot(reloj);
        assertEquals(3, snap.totalClics());
        assertEquals(1, snap.usuariosActivos());
        assertEquals(2L, snap.eventosPorTipo().get("PRODUCTO_VISTO"));
        assertEquals("Naruto", snap.productosMasVistos().get(0).nombre());
        assertEquals(10, snap.clicsPorMinuto().size());
    }

    @Test
    void verDosProductosEnLaMismaSesionLosRelaciona() {
        processor.procesar(ver("u1", 1, "Naruto", "24990"));
        processor.procesar(ver("u1", 2, "Snorlax", "39990"));

        assertEquals(1L, store.coVisualizacionesDe(1L).get(2L));
        assertEquals(1L, store.coVisualizacionesDe(2L).get(1L));
    }

    @Test
    void recomiendaLoQueOtrosUsuariosVieronJuntoAlProductoActual() {
        // Usuarios previos vieron 1 y 3 juntos
        processor.procesar(ver("u2", 1, "Naruto", "24990"));
        processor.procesar(ver("u2", 3, "Katana", "119990"));
        processor.procesar(ver("u3", 1, "Naruto", "24990"));
        processor.procesar(ver("u3", 3, "Katana", "119990"));
        processor.procesar(ver("u3", 2, "Snorlax", "39990"));   // 2 relacionado solo con 3 (y por orden con 1 no)

        // Nuevo usuario mira el producto 1
        processor.procesar(ver("u1", 1, "Naruto", "24990"));
        List<RecomendacionDTO> recs = recomendaciones.recomendar("u1", 1L, 2);

        assertFalse(recs.isEmpty());
        assertEquals(3L, recs.get(0).productoId());
        assertTrue(recs.stream().noneMatch(r -> r.productoId() == 1L), "No debe recomendar el producto que ya mira");
    }

    @Test
    void recomiendaAunqueElUsuarioYaHayaVistoTodoElCatalogo() {
        processor.procesar(ver("u1", 1, "Naruto", "24990"));
        processor.procesar(ver("u1", 2, "Snorlax", "39990"));
        processor.procesar(ver("u1", 3, "Katana", "119990"));

        List<RecomendacionDTO> recs = recomendaciones.recomendar("u1", 1L, 2);

        assertEquals(2, recs.size());
        assertTrue(recs.stream().noneMatch(r -> r.productoId() == 1L));
    }

    @Test
    void sinHistorialRecomiendaPopulares() {
        processor.procesar(ver("u2", 5, "Figura", "10000"));
        processor.procesar(ver("u3", 5, "Figura", "10000"));
        processor.procesar(ver("u3", 6, "Otra", "99999"));

        List<RecomendacionDTO> recs = recomendaciones.recomendar("nuevo", null, 2);
        assertEquals(5L, recs.get(0).productoId());
    }

    @Test
    void lasAlertasDeFraudeSeCuentan() {
        metricas.registrarAlertaFraude("tarjeta:4242", 4, 1L);
        MetricasSnapshot snap = metricas.snapshot(reloj);
        assertEquals(1, snap.alertasFraude().total());
        assertEquals("tarjeta:4242", snap.alertasFraude().recientes().get(0).claveEvaluada());
    }

    @Test
    void similitudDePrecio() {
        assertEquals(1.0, RecomendacionService.similitudPrecio(new BigDecimal("100"), new BigDecimal("100")), 1e-9);
        assertEquals(0.5, RecomendacionService.similitudPrecio(new BigDecimal("100"), new BigDecimal("50")), 1e-9);
    }
}
