package com.backend.bff.service.impl;

import com.backend.bff.client.CarritoClient;
import com.backend.bff.client.ProductoClient;
import com.backend.bff.dto.*;
import com.backend.bff.service.OrquestadorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrquestadorServiceImpl implements OrquestadorService {

    private final CarritoClient carritoClient;
    private final ProductoClient productoClient;

    public OrquestadorServiceImpl(CarritoClient carritoClient, ProductoClient productoClient) {
        this.carritoClient = carritoClient;
        this.productoClient = productoClient;
    }

    @Override
    public CarritoEnriquecidoDTO obtenerCarritoCompleto() {
        CarritoDTO carrito = carritoClient.obtenerCarritoActivo();
        return enriquecerCarrito(carrito);
    }

    @Override
    public CarritoEnriquecidoDTO agregarItemYEnriquecer(ItemCarritoRequestDTO requestDTO) {
        CarritoDTO carritoActualizado = carritoClient.agregarItem(requestDTO);
        return enriquecerCarrito(carritoActualizado);
    }

    @Override
    public CarritoEnriquecidoDTO eliminarItemYEnriquecer(Long productoId) {
        CarritoDTO carritoActualizado = carritoClient.eliminarItem(productoId);
        return enriquecerCarrito(carritoActualizado);
    }

    @Override
    public void vaciarCarrito() {
        carritoClient.vaciarCarrito();
    }

    private CarritoEnriquecidoDTO enriquecerCarrito(CarritoDTO carrito) {
        if (carrito == null || carrito.items() == null || carrito.items().isEmpty()) {
            return new CarritoEnriquecidoDTO(
                    carrito != null ? carrito.id() : null,
                    carrito != null ? carrito.usuarioId() : null,
                    carrito != null ? carrito.fechaCreacion() : null,
                    carrito != null ? carrito.total() : java.math.BigDecimal.ZERO,
                    carrito != null ? carrito.estado() : "ACTIVO",
                    List.of()
            );
        }

        // Optimización: Traemos la lista de productos en una sola llamada HTTP
        Map<Long, ProductoDTO> mapaProductos = productoClient.listarProductosActivos().stream()
                .collect(Collectors.toMap(ProductoDTO::id, Function.identity(), (p1, p2) -> p1));

        List<ItemCarritoEnriquecidoDTO> itemsEnriquecidos = carrito.items().stream()
                .map(item -> {
                    ProductoDTO prod = mapaProductos.get(item.productoId());
                    String nombre = (prod != null) ? prod.nombre() : "Producto no disponible";
                    String descripcion = (prod != null) ? prod.descripcion() : "Sin descripción";

                    return new ItemCarritoEnriquecidoDTO(
                            item.id(),
                            item.productoId(),
                            nombre,
                            descripcion,
                            item.cantidad(),
                            item.precioUnitario(),
                            item.subtotal()
                    );
                })
                .toList();

        return new CarritoEnriquecidoDTO(
                carrito.id(),
                carrito.usuarioId(),
                carrito.fechaCreacion(),
                carrito.total(),
                carrito.estado(),
                itemsEnriquecidos
        );
    }
}