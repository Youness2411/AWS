# syntax=docker/dockerfile:1.6

# Étape 1 : Build avec Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

ARG SKIP_CORP_CERT=false

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

RUN --mount=type=secret,id=corp_ca,required=false bash -euxo pipefail -c '\
  if [ "$SKIP_CORP_CERT" = "true" ]; then \
    echo "Pas de certificat pour la CI de main.yml"; \
  elif [ -f /run/secrets/corp_ca ]; then \
    install -m 0644 -D /run/secrets/corp_ca /usr/local/share/ca-certificates/corporate.crt; \
    update-ca-certificates; \
    keytool="$JAVA_HOME/bin/keytool"; \
    "$keytool" -delete -alias corporate-root -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit || true; \
    "$keytool" -importcert -noprompt -storepass changeit -keystore "$JAVA_HOME/lib/security/cacerts" -alias corporate-root -file /usr/local/share/ca-certificates/corporate.crt; \
  else \
    echo "No corporate CA secret provided, continuing"; \
  fi'

RUN mvn -B -ntp clean package -DskipTests

# Étape 2 : Image finale pour exécution
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
