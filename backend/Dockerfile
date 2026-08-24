# ==============================================================================
# Multi-stage Build for OptiMiam Spring Boot Backend
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build stage
# ------------------------------------------------------------------------------
FROM docker.io/library/eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper files and pom.xml first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies in advance to optimize Docker layer cache
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Runtime stage
# ------------------------------------------------------------------------------
FROM docker.io/library/eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create a dedicated non-root user and group
RUN addgroup -S optimiam && adduser -S optimiam -G optimiam

# Copy compiled jar from builder stage
COPY --from=builder --chown=optimiam:optimiam /app/target/optimiam-*.jar /app/app.jar

# Run as non-root user
USER optimiam

# Expose backend service port
EXPOSE 8080

# Configure container healthcheck using Actuator health endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Launch application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
