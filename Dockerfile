# Etapa 1: build del jar con Maven (usa la caché de dependencias entre builds)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: imagen de ejecución, liviana (solo JRE)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
