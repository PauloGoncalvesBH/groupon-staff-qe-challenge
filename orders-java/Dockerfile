FROM gradle:8.5-jdk21 AS builder
# Stage 1: Build the application using Gradle
WORKDIR /build

# Copy only build file to leverage Docker cache for dependencies
COPY build.gradle ./
RUN gradle --no-daemon build --dry-run

COPY . .
RUN gradle clean build --no-daemon

FROM eclipse-temurin:21-jdk
# Stage 2: Create a minimal runtime image
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
RUN useradd -ms /bin/bash appuser
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
