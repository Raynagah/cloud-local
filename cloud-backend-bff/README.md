# Pedidos360 - Backend For Frontend (BFF)

Microservicio **Backend-For-Frontend (BFF)** de la arquitectura **Pedidos360**. Actúa como orquestador e intermediario entre la aplicación de React y los microservicios internos (`ms-producto` y `ms-carrito`). Su propósito principal es agregar y enriquecer la información del carrito de compras con las descripciones y nombres de los productos, reduciendo la cantidad de peticiones HTTP en el frontend.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.2.x
* **Comunicación Interna:** Spring Cloud OpenFeign
* **Seguridad:** OAuth2 Resource Server (Token Relay hacia Azure AD)
* **Contenedorización:** Docker

## 🚀 Arquitectura y Seguridad (Token Relay)

El BFF valida los tokens JWT emitidos por Azure AD enviados por React. Mediante `FeignClientInterceptor`, intercepta cada petición saliente y reinyecta automáticamente la cabecera `Authorization: Bearer <token>` a los microservicios internos, garantizando seguridad *stateless* de extremo a extremo.

### Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/v1/bff/carritos/{usuarioId}` | Obtiene el carrito del usuario enriquecido con datos del producto |
| `POST` | `/api/v1/bff/carritos/{usuarioId}/items` | Agrega un ítem al carrito y retorna el carrito enriquecido |

## ⚙️ Instalación y Ejecución

### Requisitos Previos

* JDK 17
* Maven 3.8+
* Docker

### Variables de Entorno

| Variable | Valor por Defecto / Descripción |
| :--- | :--- |
| `AZURE_TENANT_ID` | `78b145ef-56b9-4397-b87c-27b242a9fce5` |
| `MS_PRODUCTO_URL` | `http://localhost:8082` (o IP/Nombre Docker de ms-producto) |
| `MS_CARRITO_URL` | `http://localhost:8083` (o IP/Nombre Docker de ms-carrito) |

### Compilación Local

```bash
mvn clean package -DskipTests
```

### Despliegue con Docker

1. **Construir la imagen:**

```bash
docker build -t pedidos360/bff:v1 .
```

2. **Ejecutar contenedor:**

```bash
docker run -d \
  --name bff \
  -p 8084:8084 \
  -e AZURE_TENANT_ID="78b145ef-56b9-4397-b87c-27b242a9fce5" \
  -e MS_PRODUCTO_URL="http://<IP_PRIVADA_O_DOCKER_PRODUCTO>:8082" \
  -e MS_CARRITO_URL="http://<IP_PRIVADA_O_DOCKER_CARRITO>:8083" \
  pedidos360/bff:v1
```

---

## 🔗 Ecosistema de Repositorios

### Backend

* [BFF (Este repositorio)](https://github.com/Raynagah/cloud-backend-bff)
* [Microservicio Producto](https://github.com/Raynagah/cloud-backend-producto)
* [Microservicio Carrito](https://github.com/Raynagah/cloud-backend-carrito)
* [Microservicio Usuarios](https://github.com/NBello26/ms-usuarios-cloud.git)
* [Microservicio Base de Datos](https://github.com/NBello26/ms-bd-cloud)


### Frontend

* [Frontend React](https://github.com/Raynagah/cloud-frontend.git)
