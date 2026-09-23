# Pedidos360 - Base de Datos

Repositorio encargado de la configuración, creación y carga inicial de las bases de datos utilizadas por la plataforma **Pedidos360**. Contiene el script SQL necesario para crear las bases de datos correspondientes a los microservicios de usuarios, productos y carrito, además de sus respectivas tablas y datos iniciales.

## 🛠️ Tecnologías Utilizadas

* **Motor de Base de Datos:** PostgreSQL 15
* **Contenedorización:** Docker
* **Imagen Base:** `postgres:15-alpine`
* **Lenguaje de consultas:** SQL
* **Persistencia:** PostgreSQL

## 🗄️ Bases de Datos

El proyecto utiliza una base de datos independiente para cada microservicio:

| Base de Datos  | Microservicio | Descripción                                         |
| :------------- | :------------ | :-------------------------------------------------- |
| `db_usuarios`  | Usuarios      | Almacena la información de los usuarios registrados |
| `db_productos` | Productos     | Almacena productos, precios, stock y estado         |
| `db_carrito`   | Carrito       | Administra los carritos de compra y sus productos   |

## 📋 Estructura de las Tablas

### 👤 Usuarios

Base de datos: `db_usuarios`

Tabla principal: `usuarios`

| Campo          | Tipo         | Descripción                             |
| :------------- | :----------- | :-------------------------------------- |
| `id`           | BIGINT       | Identificador único del usuario         |
| `correo`       | VARCHAR(255) | Correo electrónico único                |
| `direccion`    | VARCHAR(255) | Dirección del usuario                   |
| `edad`         | INTEGER      | Edad del usuario, entre 18 y 100 años   |
| `foto_url`     | VARCHAR(255) | URL de la fotografía                    |
| `genero`       | VARCHAR(255) | Género del usuario                      |
| `nombre`       | VARCHAR(50)  | Nombre del usuario                      |
| `ocupacion`    | VARCHAR(255) | Ocupación del usuario                   |
| `telefono`     | VARCHAR(255) | Teléfono de contacto                    |
| `tipo_usuario` | VARCHAR(100) | Tipo de usuario dentro de la plataforma |

### 📦 Productos

Base de datos: `db_productos`

Tabla principal: `productos`

| Campo                 | Tipo          | Descripción                       |
| :-------------------- | :------------ | :-------------------------------- |
| `id`                  | BIGINT        | Identificador único del producto  |
| `nombre`              | VARCHAR(100)  | Nombre del producto               |
| `descripcion`         | VARCHAR(500)  | Descripción del producto          |
| `precio`              | NUMERIC(10,2) | Precio del producto               |
| `stock`               | INTEGER       | Cantidad disponible               |
| `activo`              | BOOLEAN       | Indica si el producto está activo |
| `fecha_creacion`      | TIMESTAMP     | Fecha de creación                 |
| `fecha_actualizacion` | TIMESTAMP     | Fecha de última actualización     |

### 🛒 Carrito

Base de datos: `db_carrito`

El carrito utiliza dos tablas relacionadas:

**Tabla `carritos`**

| Campo            | Tipo          | Descripción                                       |
| :--------------- | :------------ | :------------------------------------------------ |
| `id`             | BIGINT        | Identificador único del carrito                   |
| `usuario_id`     | VARCHAR(255)  | Identificador del usuario proveniente de Azure AD |
| `fecha_creacion` | TIMESTAMP     | Fecha de creación del carrito                     |
| `total`          | NUMERIC(10,2) | Total acumulado del carrito                       |
| `estado`         | VARCHAR(50)   | Estado actual del carrito                         |

**Tabla `items_carrito`**

| Campo             | Tipo          | Descripción                        |
| :---------------- | :------------ | :--------------------------------- |
| `id`              | BIGINT        | Identificador único del ítem       |
| `carrito_id`      | BIGINT        | Identificador del carrito asociado |
| `producto_id`     | BIGINT        | Identificador del producto         |
| `cantidad`        | INTEGER       | Cantidad solicitada                |
| `precio_unitario` | NUMERIC(10,2) | Precio unitario del producto       |
| `subtotal`        | NUMERIC(10,2) | Subtotal correspondiente al ítem   |

La tabla `items_carrito` mantiene una relación mediante clave foránea con `carritos`. Al eliminar un carrito, sus ítems asociados también son eliminados mediante `ON DELETE CASCADE`.

## ⚙️ Instalación y Ejecución

### Requisitos Previos

* Docker
* PostgreSQL 15 (opcional si se ejecuta mediante Docker)
* Git

### 📁 Estructura del Proyecto

```text
.
├── Dockerfile
├── init-dbs.sql
└── README.md
```

### 🐘 Ejecución con Docker

El proyecto utiliza la imagen oficial de PostgreSQL 15 sobre Alpine.

El `Dockerfile` configura las credenciales por defecto y copia automáticamente el archivo `init-dbs.sql` al directorio de inicialización de PostgreSQL.

```dockerfile
FROM postgres:15-alpine

EXPOSE 5432

ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=admin

COPY init-dbs.sql /docker-entrypoint-initdb.d/
```

### Construir la imagen

Desde la raíz del proyecto:

```bash
docker build -t pedidos360/bd:v1 .
```

### Ejecutar el contenedor

```bash
docker run -d \
  --name pedidos360-db \
  -p 5432:5432 \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  pedidos360/bd:v1
```

El script `init-dbs.sql` será ejecutado automáticamente por PostgreSQL durante la inicialización del contenedor, creando las bases de datos, tablas y registros iniciales.

### 🔑 Credenciales por Defecto

| Variable            | Valor                               |
| :------------------ | :---------------------------------- |
| `POSTGRES_USER`     | `admin`                             |
| `POSTGRES_PASSWORD` | `admin`                             |
| `POSTGRES_DB`       | Base de datos inicial de PostgreSQL |
| `POSTGRES_PORT`     | `5432`                              |

> **Nota:** Las credenciales mostradas corresponden a valores de desarrollo. Para ambientes de producción se recomienda utilizar variables de entorno seguras y no almacenar credenciales directamente en el repositorio.

## 📝 Script de Inicialización

El archivo `init-dbs.sql` contiene todas las instrucciones necesarias para preparar la infraestructura de datos de **Pedidos360**.

El script realiza las siguientes operaciones:

1. Creación de `db_usuarios`.
2. Creación de la tabla `usuarios`.
3. Inserción de usuarios iniciales.
4. Actualización de la secuencia de identificadores.
5. Creación de `db_productos`.
6. Creación de la tabla `productos`.
7. Inserción de productos iniciales.
8. Actualización de la secuencia de identificadores.
9. Creación de `db_carrito`.
10. Creación de las tablas `carritos` e `items_carrito`.
11. Configuración de la relación entre carritos e ítems mediante clave foránea.

## 🔗 Conexión desde los Microservicios

Los microservicios se conectan a sus respectivas bases de datos mediante PostgreSQL:

```text
Pedidos360
│
├── ms-usuarios ──────► db_usuarios
│
├── ms-productos ─────► db_productos
│
└── ms-carrito ───────► db_carrito
```

Cada microservicio mantiene su propia base de datos, siguiendo un enfoque desacoplado para la arquitectura de microservicios.

### Ejemplo de conexión

```properties
spring.datasource.url=jdbc:postgresql://<HOST_BD>:5432/db_usuarios
spring.datasource.username=<USUARIO_BD>
spring.datasource.password=<PASSWORD_BD>
```

Para productos:

```properties
spring.datasource.url=jdbc:postgresql://<HOST_BD>:5432/db_productos
```

Para carrito:

```properties
spring.datasource.url=jdbc:postgresql://<HOST_BD>:5432/db_carrito
```

---

## 🔗 Ecosistema de Repositorios

### Backend

* [Microservicio Carrito](https://github.com/Raynagah/cloud-backend-carrito)
* [BFF Orchestrator](https://github.com/Raynagah/cloud-backend-bff)
* [Microservicio Producto](https://github.com/Raynagah/cloud-backend-producto)
* [Microservicio Usuarios](https://github.com/NBello26/ms-usuarios-cloud.git)

### Frontend

* [Frontend React](https://github.com/Raynagah/cloud-frontend.git)

### Base de Datos

* **Este repositorio:** Configuración y scripts de PostgreSQL para Pedidos360

---

## 🚀 Pedidos360

La base de datos forma parte del ecosistema de **Pedidos360**, proporcionando persistencia independiente para los diferentes microservicios de la plataforma.

**PostgreSQL + Docker + Microservicios = Pedidos360**
