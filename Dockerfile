## Stage 1: Build con Maven y GraalVM Native
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS build

# Copiar archivos de Maven wrapper
COPY --chown=quarkus:quarkus --chmod=0755 mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:go-offline

# Copiar código fuente
COPY --chown=quarkus:quarkus src /code/src

# Compilar a nativo
RUN ./mvnw clean package -Pnative -Dquarkus.profile=prod -DskipTests

## Stage 2: Imagen final con el ejecutable nativo
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

WORKDIR /work/

# Copiar el ejecutable nativo desde el stage de build
COPY --from=build /code/target/*-runner /work/application

# Configurar permisos para usuario 1001
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

# Ejecutar la aplicación nativa
# ${PORT:-8080} permite que Render asigne el puerto dinámicamente
CMD ["sh", "-c", "./application -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080}"]