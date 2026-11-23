# Evaluación 3 – Pedidos360: guía de cambios

Esta carpeta contiene **solo los archivos nuevos o modificados**, con la misma estructura que `cloud-local`.
Cópialos encima de tu proyecto (respetando rutas).

## ARCHIVOS A ELIMINAR de `cloud-backend-pago` (consumidores duplicados)
- `src/main/java/com/backend/pagos/component/ConsumidorComponent.java` (y la carpeta `component`)
- `src/main/java/com/backend/pagos/service/VelocidadPagoConsumer.java`
- `src/main/java/com/backend/pagos/config/ConsumidorConfig.java`

Motivo: había dos listeners en el mismo topic y mismo `groupId` (uno con ventana de 1 minuto y otro de 10 s). Kafka repartía
los mensajes entre ellos, así que la regla no se aplicaba de forma consistente. Además la detección era asíncrona, por lo que
un 4.º intento podía pasar antes de que el consumidor lo procesara.

## 1. ms-pago – antifraude (`AntifraudeService`)
- Regla: **más de 3 usos de la misma tarjeta en una ventana deslizante de 10 s** → el intento se bloquea (HTTP 429),
  se bloquea la clave 30 min, se guarda la alerta en la tabla `alerta_fraude` y se publica en Kafka (`fraud-alerts`).
- Evaluación **síncrona antes de llamar a Webpay** (bloqueo realmente preventivo).
- `PeticionPagoDTO` tiene un campo opcional `tarjeta` (hash o BIN+últimos 4; **nunca el número completo**). Con Webpay Plus
  la tarjeta se ingresa en el sitio de Transbank, por lo que el backend no la ve; si no se envía `tarjeta`, la regla usa el `clientId`.
- Un pago rechazado ya **no reinicia** el contador (antes lo hacía y permitía eludir la regla).
- `GET /api/pagos/alertas` lista las últimas 50 alertas.
- Test: `AntifraudeServiceTest`.

Prueba rápida (4 intentos seguidos; el 4.º debe responder 429):
```
for i in 1 2 3 4; do curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8088/api/pagos/iniciar \
 -H "Content-Type: application/json" -d '{"clientId":"u1","amount":1000,"transactionId":"T1","tarjeta":"4242"}'; done
```

## 2. Eventos hacia Kafka (topic `user-events`)
- `cloud-backend-producto`: `GET /api/v1/productos/{id}` publica `PRODUCTO_VISTO`.
- `cloud-backend-carrito`: publica `CARRITO_ITEM_AGREGADO`, `CARRITO_ITEM_ELIMINADO`, `CARRITO_VACIADO`, `COMPRA_REALIZADA`.
- Son *best effort*: si Kafka no responde (máx. 1 s) se registra un warning y el flujo de negocio continúa.
- Nota: el enunciado menciona "cupones" y "envíos"; hoy no existen en el código, así que no hay eventos de ellos.

## 3. ms-analytics-recommendations (nuevo, puerto 8089)
- Consume `user-events` y `fraud-alerts`.
- **DynamoDB (+2)**: tablas `pedidos360_analytics_eventos`, `_productos`, `_coview` (se crean solas). En docker-compose usa
  *DynamoDB Local*; para AWS real deja `DYNAMODB_ENDPOINT` vacío y define `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`.
- **Recomendaciones**: `GET /api/v1/analytics/recomendaciones?productoId=..` (vía BFF: `/api/v1/bff/analitica/recomendaciones`).
  Co-visualización ("quienes vieron A también vieron B") + similitud de precio + populares como respaldo.
- **WebSocket (+3)**: `ws://localhost:8089/ws/metrics?token=<JWT>` envía cada 2 s: clics totales, usuarios activos,
  productos más vistos, clics por minuto, eventos por tipo y alertas de fraude (≥3 métricas). Para demos sin token:
  `ANALYTICS_WS_REQUIRE_AUTH=false`.
- Tests: `AnalyticsServicesTest`.

## 4. Frontend y BFF
- Nueva página **Analítica** (`/desarrollo/analitica`, enlace en la barra) con el dashboard en vivo y reconexión automática.
- `ProductoDetallePage` muestra "También te podría interesar" con las recomendaciones.
- BFF: `AnalyticsClient` + `BffAnaliticaController`; variables `MS_ANALYTICS_URL`.
- Si ya tenías el frontend construido, reconstruye la imagen (`docker compose build ms-frontend`).

## 5. docker-compose
- Nuevos servicios: `dynamodb-local` y `ms-analytics`; `KAFKA_HOST` en producto y carrito; el BFF espera a `ms-analytics`.
- Corrección: el BFF leía `MS_DESPACHOS_URL` pero el compose definía `MS_DESPACHO_URL`; ahora se definen ambas.

## Pendiente / verificar al compilar
Estos cambios se escribieron **sin poder compilar** (no hubo acceso a Maven Central). El frontend sí compila (`vite build`).
Al ejecutar `mvn clean package` en cada servicio, revisa en especial:
1. `ms-analytics-recommendations/pom.xml`: nombre del starter `spring-boot-starter-websocket` y versión del BOM de AWS (`aws.sdk.version=2.29.0`).
2. Que `com.fasterxml.jackson.databind.ObjectMapper` (Jackson 2) esté disponible: se declara `jackson-databind` explícitamente, igual que ya hacía producto.
3. Los tests unitarios nuevos: `mvn test` en `cloud-backend-pago` y `ms-analytics-recommendations`.

Microservicio libre: queda fuera por ahora (confirmado). Una opción simple para más adelante es un `ms-favoritos` (lista de deseos con CRUD + evento Kafka).
