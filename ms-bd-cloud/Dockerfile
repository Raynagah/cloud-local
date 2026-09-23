FROM postgres:15-alpine

# Exponemos el puerto por buena práctica/documentación
EXPOSE 5432

# Variables por defecto (las sobreescribes luego en el pipeline con -e)
ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=admin

# Copiamos el script a la carpeta especial de inicialización de Postgres
COPY init-dbs.sql /docker-entrypoint-initdb.d/