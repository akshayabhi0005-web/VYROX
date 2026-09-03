# Multi-stage build for VYROX Spring Boot Java 21 Backend
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/vyrox-backend-1.0.0-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["sh", "-c", "java -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
