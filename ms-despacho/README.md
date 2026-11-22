# Pedidos360 - Microservicio de Despachos (`ms-despacho`)

Microservicio encargado de la gestión logística, seguimiento y procesamiento de los envíos de pedidos para la plataforma **Pedidos360**. Escucha de forma asíncrona los eventos de creación de órdenes generados por `ms-ordenes` para iniciar el ciclo de despacho y enviar confirmaciones por correo electrónico.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.2.x
* **Mensajería:** Spring AMQP / RabbitMQ (Consumidor con ACK Manual)
* **Envío de Correo:** JavaMailSender / Mailtrap
* **Persistencia:** Spring Data JPA / PostgreSQL (`db_despachos`)
* **Seguridad:** OAuth2 Resource Server (Validación JWT con Azure AD)
* **Contenedorización:** Docker

## ⚙️ Instalación y Ejecución

### Requisitos Previos

* JDK 17
* Maven 3.8+
* RabbitMQ Server
* PostgreSQL (Base de datos `db_despachos`)
* Docker

### Variables de Entorno

| Variable | Valor por Defecto / Descripción |
| :--- | :--- |
| `AZURE_TENANT_ID` | `78b145ef-56b9-4397-b87c-27b242a9fce5` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<HOST_BD>:5432/db_despachos` |
| `SPRING_DATASOURCE_USERNAME` | Credencial de base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Credencial de base de datos |
| `RABBITMQ_HOST` | Host del servidor RabbitMQ (`localhost`) |
| `RABBITMQ_USER` | Usuario de RabbitMQ (`admin`) |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ (`admin`) |
| `MAIL_USERNAME` | Usuario SMTP (Mailtrap) |
| `MAIL_PASSWORD` | Contraseña SMTP (Mailtrap) |

### Colas e Integración con RabbitMQ

| Cola Escuchada | Exchange | Routing Key | Descripción del Evento |
| :--- | :--- | :--- | :--- |
| `q.generar-despacho` | `pedidos.exchange` | `pedido.creado` | Recepción de orden creada para la generación automática del despacho |

### Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/v1/despachos` | Consulta el estado e historial de los despachos |
| `GET` | `/api/v1/despachos/{ordenId}` | Obtiene la información detallada del despacho de una orden |
| `POST` | `/api/v1/admin/queues` | Crea dinámicamente una nueva cola en RabbitMQ |
| `DELETE` | `/api/v1/admin/queues/{nombre}` | Elimina una cola existente en RabbitMQ |

### Compilación Local

```bash
mvn clean package -DskipTests
```

### Despliegue con Docker

1. **Construir la imagen:**

```bash
docker build -t pedidos360/ms-despacho:v1 .
```

2. **Ejecutar contenedor:**

```bash
docker run -d \
  --name ms-despacho \
  -p 8087:8087 \
  -e AZURE_TENANT_ID="78b145ef-56b9-4397-b87c-27b242a9fce5" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://<HOST_BD>:5432/db_despachos" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="password" \
  -e RABBITMQ_HOST="host.docker.internal" \
  -e RABBITMQ_USER="admin" \
  -e RABBITMQ_PASSWORD="admin" \
  -e MAIL_USERNAME="tu_usuario_mailtrap" \
  -e MAIL_PASSWORD="tu_password_mailtrap" \
  pedidos360/ms-despacho:v1
```

---

## 🔗 Ecosistema de Repositorios

### Backend

* [Microservicio Despacho (Este repositorio)](https://github.com/Raynagah/cloud-backend-despacho)
* [Microservicio Órdenes](https://github.com/Raynagah/cloud-backend-ordenes)
* [Microservicio Notificaciones](https://github.com/Raynagah/cloud-backend-notificaciones)
* [Microservicio Producto](https://github.com/Raynagah/cloud-backend-producto)
* [BFF Orchestrator](https://github.com/Raynagah/cloud-backend-bff)
* [Microservicio Carrito](https://github.com/Raynagah/cloud-backend-carrito)
* [Microservicio Usuarios](https://github.com/NBello26/ms-usuarios-cloud.git)
* [Microservicio Base de Datos](https://github.com/NBello26/ms-bd-cloud)

### Frontend

* [Frontend React](https://github.com/Raynagah/cloud-frontend.git)
