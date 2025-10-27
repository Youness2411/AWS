# syntax=docker/dockerfile:1.6
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

ADD certs/ /tmp/certs/
RUN --mount=type=secret,id=corp_ca,required=false bash -euxo pipefail -c '\
  if [ "${SKIP_CORP_CERT:-false}" = "true" ]; then \
    echo "Skipping corporate CA"; \
  elif [ -s /run/secrets/corp_ca ]; then \
    install -m 0644 -D /run/secrets/corp_ca /usr/local/share/ca-certificates/corporate.crt; \
    update-ca-certificates; \
    keytool="$JAVA_HOME/bin/keytool"; \
    "$keytool" -delete -alias corporate-root -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit || true; \
    "$keytool" -importcert -noprompt -storepass changeit -keystore "$JAVA_HOME/lib/security/cacerts" -alias corporate-root -file /usr/local/share/ca-certificates/corporate.crt; \
  elif [ -s /tmp/certs/SOLUTEC-RootCA_base64.crt ]; then \
    install -m 0644 -D /tmp/certs/SOLUTEC-RootCA_base64.crt /usr/local/share/ca-certificates/corporate.crt; \
    update-ca-certificates; \
    keytool="$JAVA_HOME/bin/keytool"; \
    "$keytool" -delete -alias corporate-root -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit || true; \
    "$keytool" -importcert -noprompt -storepass changeit -keystore "$JAVA_HOME/lib/security/cacerts" -alias corporate-root -file /usr/local/share/ca-certificates/corporate.crt; \
  else \
    echo "No corporate CA provided, continuing"; \
  fi'


ENV JAVA_HOME=/opt/java/openjdk

COPY pom.xml .
COPY src ./src


RUN mvn -B -ntp clean package -DskipTests

RUN mkdir -p /app/uploads/images

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
