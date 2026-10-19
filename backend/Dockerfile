FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test \
	&& find build/libs -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} /app/application.jar \; \
	&& test -f /app/application.jar

FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache wget \
	&& addgroup -S pokehub \
	&& adduser -S pokehub -G pokehub
WORKDIR /app
COPY --from=build --chown=pokehub:pokehub /app/application.jar app.jar
USER pokehub

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=90s --retries=12 \
	CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
