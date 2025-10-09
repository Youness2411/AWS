# syntax=docker/dockerfile:1.6

# Étape 1 : Build Angular
FROM node:20 AS builder
WORKDIR /app

ARG SKIP_CORP_CERT=false

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

RUN --mount=type=secret,id=corp_ca,required=false bash -euxo pipefail -c '\
  if [ "$SKIP_CORP_CERT" = "true" ]; then exit 0; fi; \
  if [ -f /run/secrets/corp_ca ]; then \
    install -m 0644 -D /run/secrets/corp_ca /usr/local/share/ca-certificates/corporate.crt; \
    update-ca-certificates; \
  fi'

COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Étape 2 : Image finale avec Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY --from=builder /app/dist/frontend/browser ./
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
