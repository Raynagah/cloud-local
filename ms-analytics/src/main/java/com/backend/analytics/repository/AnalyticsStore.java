package com.backend.analytics.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.model.ProductoStats;

/**
 * Puerto de persistencia del pipeline analítico. La implementación real usa Amazon DynamoDB;
 * al ser una interfaz, la lógica de negocio se puede probar con un almacén en memoria.
 */
public interface AnalyticsStore {

    /** Garantiza que las tablas existan (las crea si faltan). */
    void asegurarTablas();

    void guardarEvento(EventoUsuario evento);

    /** Eventos más recientes del usuario, del más nuevo al más antiguo. */
    List<EventoUsuario> ultimosEventos(String usuarioId, int limite);

    /**
     * Incrementa atómicamente un contador del producto ("vistas", "agregados", "quitados", "favoritos").
     * Si se entregan nombre/precio, se actualizan para tener datos legibles en el dashboard.
     */
    void incrementarContador(Long productoId, String contador, String nombre, BigDecimal precio);

    Optional<ProductoStats> obtenerProducto(Long productoId);

    List<ProductoStats> listarProductos();

    /** Suma 1 a la co-visualización del par (a, b). */
    void incrementarCoVisualizacion(Long productoId, Long relacionadoId);

    /** Productos vistos junto a productoId, con su puntaje acumulado. */
    Map<Long, Long> coVisualizacionesDe(Long productoId);
}
