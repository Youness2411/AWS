# syntax=docker/dockerfile:1.6 Test
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

ENV JAVA_HOME=/opt/java/openjdk

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

RUN mvn -B -ntp clean package -DskipTests

RUN mkdir -p /app/uploads/images

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
