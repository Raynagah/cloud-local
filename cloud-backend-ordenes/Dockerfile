# Etapa 1: Construcción (Build)
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiamos el pom y descargamos dependencias para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos omitiendo los tests
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la etapa de build
COPY --from=builder /app/target/*-SNAPSHOT.jar ms-ordenes.jar

# Puerto configurado para ms-ordenes
EXPOSE 8085

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "ms-ordenes.jar"]