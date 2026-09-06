# Render MCP no puede fijar rootDir; el contexto es la raíz del repo.
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline

COPY backend/src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S helpdesk && adduser -S helpdesk -G helpdesk
USER helpdesk

COPY --from=build /app/target/helpdesk-backend-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
