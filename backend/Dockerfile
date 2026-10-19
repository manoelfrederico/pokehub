# Stage 1: Build the Java application
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy Gradle wrapper and build configuration files
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/

# Download dependencies (cached if build files don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code and build the JAR package
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]