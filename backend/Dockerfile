# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Gradle wrapper and build descriptor first for dependency caching
COPY gradlew gradlew.bat settings.gradle build.gradle.kts ./
COPY gradle gradle/
RUN chmod +x gradlew

# Pre-fetch dependencies as a separate layer (invalidated only on build file changes)
RUN ./gradlew --no-daemon dependencies

# Copy source and build the fat JAR (skip tests — H2 tests don't need Postgres)
COPY src src/
RUN ./gradlew --no-daemon bootJar -x test

# Stage 2: Runtime — slim JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
