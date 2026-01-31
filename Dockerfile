# ===== BUILD =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar los archivos de configuración de Maven
COPY pom.xml .

# Descargar las dependencias (esto se cachea si el pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar la aplicación
RUN mvn clean package -DskipTests

#===== RUNTIME =====
#Imagen base que usa JRE en lugar de JDK (mas liviano)
FROM eclipse-temurin:21-jre-alpine

#Directorio de Trabajo
WORKDIR /app

#Copiar el JAR
COPY --from=build /app/target/*.jar app.jar

#Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto (pueden ser sobrescritas por docker-compose)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/citas_db?createDatabaseIfNotExist=true&serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=123456

#Comando para ejecutar la aplicacion
ENTRYPOINT ["java", "-jar", "app.jar"]