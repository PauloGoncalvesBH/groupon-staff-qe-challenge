FROM gradle:8.5-jdk21@sha256:873def2b8a73d00f5616043ac9ff65576b055942701391ad66499f1a1f53f1b2 AS builder
# Stage 1: Build the application using Gradle
WORKDIR /build

# Copy only build file to leverage Docker cache for dependencies
COPY build.gradle ./
RUN gradle --no-daemon build --dry-run

COPY . .
RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jdk@sha256:40f9bb2bba5571ed0bb8d16f089afd2dbabb0d5fd727767c311330b8c604f4b0
# Stage 2: Create a minimal runtime image
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
RUN useradd -ms /bin/bash appuser
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
