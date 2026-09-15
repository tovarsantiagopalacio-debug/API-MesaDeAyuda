FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests -B

ENTRYPOINT ["java", "-jar", "/app/target/security-0.0.1-SNAPSHOT.jar"]