FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app

# Update CA certificates inside container (fix for PKIX)
USER root
RUN apt-get update && \
    apt-get install -y ca-certificates && \
    update-ca-certificates

# Copy project files
COPY . .

# gradlew executable
RUN chmod +x gradlew

# Build using wrapper
RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
