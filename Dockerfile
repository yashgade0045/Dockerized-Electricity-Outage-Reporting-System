# ============================================================
# Electricity Outage Reporting System — Dockerfile
# Multi-stage build:
#   Stage 1 (builder): Compile and package the JAR with Maven
#   Stage 2 (runtime): Run the JAR on Eclipse Temurin JRE 21
# ============================================================

# ── Stage 1: Build ─────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POM first (improves Docker layer caching)
COPY .mvn .mvn
COPY mvnw mvnw.cmd pom.xml ./

# Download dependencies in a separate layer (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy source code and build the application
COPY src ./src
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ───────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the packaged JAR from the builder stage
COPY --from=builder /build/target/outage-reporting-*.jar app.jar

# Set file ownership to the non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the application port
EXPOSE 8080

# Health check — polls the Spring Boot Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Start the application
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
