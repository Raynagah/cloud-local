# Pedidos360 - Microservicio de Órdenes (`ms-ordenes`)

Microservicio encargado de la creación, consulta y gestión del ciclo de vida de las órdenes de compra para la plataforma **Pedidos360**. Actúa como emisor/productor principal de eventos en la arquitectura orientada a eventos, notificando a los demás servicios (`ms-despacho`, `ms-carrito`, `ms-notificaciones`) mediante RabbitMQ tras la generación de un pedido.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.2.x
* **Mensajería:** Spring AMQP / RabbitMQ (Productor de Eventos)
* **Persistencia:** Spring Data JPA / PostgreSQL (`db_ordenes`)
* **Seguridad:** OAuth2 Resource Server (Validación JWT con Azure AD)
* **Contenedorización:** Docker

## ⚙️ Instalación y Ejecución

### Requisitos Previos

* JDK 17
* Maven 3.8+
* RabbitMQ Server
* PostgreSQL (Base de datos `db_ordenes`)
* Docker

### Variables de Entorno

| Variable | Valor por Defecto / Descripción |
| :--- | :--- |
| `AZURE_TENANT_ID` | `78b145ef-56b9-4397-b87c-27b242a9fce5` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<HOST_BD>:5432/db_ordenes` |
| `SPRING_DATASOURCE_USERNAME` | Credencial de base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Credencial de base de datos |
| `RABBITMQ_HOST` | Host del servidor RabbitMQ (`localhost`) |
| `RABBITMQ_USER` | Usuario de RabbitMQ (`admin`) |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ (`admin`) |

### Eventos Publicados en RabbitMQ

| Evento / DTO | Exchange | Routing Key | Servicios Consumidores |
| :--- | :--- | :--- | :--- |
| `OrdenCreadaEvent` | `pedidos.exchange` | `pedido.creado` | `ms-despacho`, `ms-carrito`, `ms-notificaciones` |

### Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/ordenes` | Registra una nueva orden de compra y emite el evento `pedido.creado` |
| `GET` | `/api/v1/ordenes` | Obtiene el historial de órdenes del usuario autenticado |
| `GET` | `/api/v1/ordenes/{id}` | Obtiene el detalle específico de una orden por ID |
| `POST` | `/api/v1/admin/queues` | Crea dinámicamente una nueva cola en RabbitMQ |
| `DELETE` | `/api/v1/admin/queues/{nombre}` | Elimina una cola existente en RabbitMQ |

### Compilación Local

```bash
mvn clean package -DskipTests
```

### Despliegue con Docker

1. **Construir la imagen:**

```bash
docker build -t pedidos360/ms-ordenes:v1 .
```

2. **Ejecutar contenedor:**

```bash
docker run -d \
  --name ms-ordenes \
  -p 8081:8081 \
  -e AZURE_TENANT_ID="78b145ef-56b9-4397-b87c-27b242a9fce5" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://<HOST_BD>:5432/db_ordenes" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="password" \
  -e RABBITMQ_HOST="host.docker.internal" \
  -e RABBITMQ_USER="admin" \
  -e RABBITMQ_PASSWORD="admin" \
  pedidos360/ms-ordenes:v1
```

---

## 🔗 Ecosistema de Repositorios

### Backend

* [Microservicio Órdenes (Este repositorio)](https://github.com/Raynagah/cloud-backend-ordenes)
* [Microservicio Notificaciones](https://github.com/Raynagah/cloud-backend-notificaciones)
* [Microservicio Producto](https://github.com/Raynagah/cloud-backend-producto)
* [BFF Orchestrator](https://github.com/Raynagah/cloud-backend-bff)
* [Microservicio Carrito](https://github.com/Raynagah/cloud-backend-carrito)
* [Microservicio Usuarios](https://github.com/NBello26/ms-usuarios-cloud.git)
* [Microservicio Base de Datos](https://github.com/NBello26/ms-bd-cloud)

### Frontend

* [Frontend React](https://github.com/Raynagah/cloud-frontend.git)
