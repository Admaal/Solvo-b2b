# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# Runtime — fat JAR sobre JRE Alpine (~210 MB en Artifact Registry)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S helpdesk && adduser -S helpdesk -G helpdesk
USER helpdesk

COPY --from=build /app/target/helpdesk-backend-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
