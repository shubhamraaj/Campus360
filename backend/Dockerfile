# Stage 1: Build the JAR package using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java Runtime environment for running the service
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/target/campus360-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]