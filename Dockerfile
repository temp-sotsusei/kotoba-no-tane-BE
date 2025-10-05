# syntax=docker/dockerfile:1.6

FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
COPY app/ ./app/
RUN gradle -p app clean build --no-daemon

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
