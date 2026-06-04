# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the executable fat jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first (only re-downloads when pom.xml changes)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the shaded jar (skip tests for a faster image build)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage: small JRE image with just the jar ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/campus-payment-platform.jar app.jar

# DB settings are read from environment variables at runtime, e.g.:
#   docker run --rm -it \
#     -e DB_URL="jdbc:postgresql://host/db?sslmode=require" \
#     -e DB_USER="user" -e DB_PASSWORD="pass" \
#     campus-payment-platform
ENTRYPOINT ["java", "-jar", "app.jar"]
