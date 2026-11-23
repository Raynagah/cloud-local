package com.backend.analytics.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.backend.analytics.model.EventoUsuario;
import com.backend.analytics.model.ProductoStats;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

/**
 * Implementación del almacén analítico sobre Amazon DynamoDB.
 *
 * Tablas (prefijo configurable, por defecto "pedidos360_"):
 *  - analytics_eventos   : PK usuarioId (S)  + SK sk (S = "<epochMs 13 dígitos>#<eventId>")  -> historial de eventos
 *  - analytics_productos : PK productoId (S)                                                  -> contadores por producto
 *  - analytics_coview    : PK productoId (S)  + SK relacionadoId (S)                          -> co-visualizaciones
 */
@Slf4j
@Repository
public class DynamoAnalyticsStore implements AnalyticsStore {

    private final DynamoDbClient dynamo;
    private final String tablaEventos;
    private final String tablaProductos;
    private final String tablaCoView;

    public DynamoAnalyticsStore(DynamoDbClient dynamo,
                                @Value("${analytics.dynamodb.table-prefix:pedidos360_}") String prefijo) {
        this.dynamo = dynamo;
        this.tablaEventos = prefijo + "analytics_eventos";
        this.tablaProductos = prefijo + "analytics_productos";
        this.tablaCoView = prefijo + "analytics_coview";
    }

    // ------------------------------------------------------------------ tablas

    @Override
    public void asegurarTablas() {
        crearSiNoExiste(tablaEventos, "usuarioId", "sk");
        crearSiNoExiste(tablaProductos, "productoId", null);
        crearSiNoExiste(tablaCoView, "productoId", "relacionadoId");
    }

    private void crearSiNoExiste(String tabla, String pk, String sk) {
        try {
            dynamo.describeTable(DescribeTableRequest.builder().tableName(tabla).build());
            return;
        } catch (ResourceNotFoundException e) {
            log.info("Tabla DynamoDB {} no existe; creándola...", tabla);
        }

        List<KeySchemaElement> claves = new ArrayList<>();
        List<AttributeDefinition> atributos = new ArrayList<>();
        claves.add(KeySchemaElement.builder().attributeName(pk).keyType(KeyType.HASH).build());
        atributos.add(AttributeDefinition.builder().attributeName(pk).attributeType(ScalarAttributeType.S).build());
        if (sk != null) {
            claves.add(KeySchemaElement.builder().attributeName(sk).keyType(KeyType.RANGE).build());
            atributos.add(AttributeDefinition.builder().attributeName(sk).attributeType(ScalarAttributeType.S).build());
        }

        dynamo.createTable(CreateTableRequest.builder()
                .tableName(tabla)
                .keySchema(claves)
                .attributeDefinitions(atributos)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        log.info("Tabla DynamoDB {} creada.", tabla);
    }

    // ------------------------------------------------------------------ eventos

    @Override
    public void guardarEvento(EventoUsuario e) {
        long ts = e.getTimestamp() != null ? e.getTimestamp() : System.currentTimeMillis();
        String id = e.getEventId() != null ? e.getEventId() : java.util.UUID.randomUUID().toString();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("usuarioId", s(e.getUsuarioId() != null ? e.getUsuarioId() : "anonimo"));
        item.put("sk", s(String.format("%013d#%s", ts, id)));
        item.put("tipo", s(e.getTipo()));
        item.put("ts", n(ts));
        if (e.getProductoId() != null) item.put("productoId", n(e.getProductoId()));
        if (e.getProductoNombre() != null) item.put("productoNombre", s(e.getProductoNombre()));
        if (e.getPrecio() != null) item.put("precio", AttributeValue.fromN(e.getPrecio().toPlainString()));
        if (e.getCantidad() != null) item.put("cantidad", n(e.getCantidad()));
        if (e.getOrigen() != null) item.put("origen", s(e.getOrigen()));

        dynamo.putItem(PutItemRequest.builder().tableName(tablaEventos).item(item).build());
    }

    @Override
    public List<EventoUsuario> ultimosEventos(String usuarioId, int limite) {
        var respuesta = dynamo.query(QueryRequest.builder()
                .tableName(tablaEventos)
                .keyConditionExpression("usuarioId = :u")
                .expressionAttributeValues(Map.of(":u", s(usuarioId)))
                .scanIndexForward(false)   // más recientes primero
                .limit(limite)
                .build());

        List<EventoUsuario> lista = new ArrayList<>();
        for (Map<String, AttributeValue> it : respuesta.items()) {
            lista.add(EventoUsuario.builder()
                    .usuarioId(str(it, "usuarioId"))
                    .tipo(str(it, "tipo"))
                    .productoId(lng(it, "productoId"))
                    .productoNombre(str(it, "productoNombre"))
                    .precio(dec(it, "precio"))
                    .cantidad(lng(it, "cantidad") != null ? lng(it, "cantidad").intValue() : null)
                    .origen(str(it, "origen"))
                    .timestamp(lng(it, "ts"))
                    .build());
        }
        return lista;
    }

    // ------------------------------------------------------------------ productos

    @Override
    public void incrementarContador(Long productoId, String contador, String nombre, BigDecimal precio) {
        Map<String, String> nombres = new HashMap<>();
        Map<String, AttributeValue> valores = new HashMap<>();
        nombres.put("#c", contador);
        valores.put(":uno", n(1));

        StringBuilder expr = new StringBuilder();
        List<String> sets = new ArrayList<>();
        if (nombre != null) {
            nombres.put("#n", "nombre");
            valores.put(":nombre", s(nombre));
            sets.add("#n = :nombre");
        }
        if (precio != null) {
            nombres.put("#p", "precio");
            valores.put(":precio", AttributeValue.fromN(precio.toPlainString()));
            sets.add("#p = :precio");
        }
        if (!sets.isEmpty()) {
            expr.append("SET ").append(String.join(", ", sets)).append(" ");
        }
        expr.append("ADD #c :uno");   // ADD crea el atributo en 0 si no existe => incremento atómico

        dynamo.updateItem(UpdateItemRequest.builder()
                .tableName(tablaProductos)
                .key(Map.of("productoId", s(String.valueOf(productoId))))
                .updateExpression(expr.toString())
                .expressionAttributeNames(nombres)
                .expressionAttributeValues(valores)
                .build());
    }

    @Override
    public Optional<ProductoStats> obtenerProducto(Long productoId) {
        var r = dynamo.getItem(GetItemRequest.builder()
                .tableName(tablaProductos)
                .key(Map.of("productoId", s(String.valueOf(productoId))))
                .consistentRead(true)
                .build());
        if (!r.hasItem() || r.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(aStats(r.item()));
    }

    @Override
    public List<ProductoStats> listarProductos() {
        List<ProductoStats> lista = new ArrayList<>();
        // Catálogo pequeño: un Scan paginado es suficiente
        for (Map<String, AttributeValue> it : dynamo.scanPaginator(
                ScanRequest.builder().tableName(tablaProductos).build()).items()) {
            lista.add(aStats(it));
        }
        return lista;
    }

    // ------------------------------------------------------------------ co-visualización

    @Override
    public void incrementarCoVisualizacion(Long productoId, Long relacionadoId) {
        dynamo.updateItem(UpdateItemRequest.builder()
                .tableName(tablaCoView)
                .key(Map.of(
                        "productoId", s(String.valueOf(productoId)),
                        "relacionadoId", s(String.valueOf(relacionadoId))))
                .updateExpression("ADD #s :uno")
                .expressionAttributeNames(Map.of("#s", "score"))
                .expressionAttributeValues(Map.of(":uno", n(1)))
                .build());
    }

    @Override
    public Map<Long, Long> coVisualizacionesDe(Long productoId) {
        Map<Long, Long> resultado = new LinkedHashMap<>();
        for (Map<String, AttributeValue> it : dynamo.queryPaginator(QueryRequest.builder()
                .tableName(tablaCoView)
                .keyConditionExpression("productoId = :p")
                .expressionAttributeValues(Map.of(":p", s(String.valueOf(productoId))))
                .build()).items()) {
            Long relacionado = Long.valueOf(str(it, "relacionadoId"));
            Long score = lng(it, "score");
            resultado.put(relacionado, score != null ? score : 0L);
        }
        return resultado;
    }

    // ------------------------------------------------------------------ helpers

    private ProductoStats aStats(Map<String, AttributeValue> it) {
        Long vistas = lng(it, "vistas");
        Long agregados = lng(it, "agregados");
        Long quitados = lng(it, "quitados");
        Long favoritos = lng(it, "favoritos");
        return new ProductoStats(
                Long.valueOf(str(it, "productoId")),
                str(it, "nombre"),
                dec(it, "precio"),
                vistas != null ? vistas : 0L,
                agregados != null ? agregados : 0L,
                quitados != null ? quitados : 0L,
                favoritos != null ? favoritos : 0L);
    }

    private static AttributeValue s(String v) {
        return AttributeValue.fromS(v);
    }

    private static AttributeValue n(long v) {
        return AttributeValue.fromN(Long.toString(v));
    }

    private static String str(Map<String, AttributeValue> it, String k) {
        AttributeValue v = it.get(k);
        return v != null ? v.s() : null;
    }

    private static Long lng(Map<String, AttributeValue> it, String k) {
        AttributeValue v = it.get(k);
        return (v != null && v.n() != null) ? Long.valueOf(new BigDecimal(v.n()).longValue()) : null;
    }

    private static BigDecimal dec(Map<String, AttributeValue> it, String k) {
        AttributeValue v = it.get(k);
        return (v != null && v.n() != null) ? new BigDecimal(v.n()) : null;
    }
}
