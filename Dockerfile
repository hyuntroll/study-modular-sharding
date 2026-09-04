# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY . .

RUN chmod +x gradlew \
    && ./gradlew clean bootJar --no-daemon


FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

WORKDIR /app

COPY --from=builder \
     --chown=app:app \
     /workspace/build/libs/application.jar \
     /app/application.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
