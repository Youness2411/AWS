# syntax=docker/dockerfile:1.6

FROM node:20 AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

ADD certs/ /tmp/certs/
RUN --mount=type=secret,id=corp_ca,required=false bash -euxo pipefail -c '\
  if [ "${SKIP_CORP_CERT:-false}" = "true" ]; then \
    echo "Skipping corporate CA"; \
  elif [ -s /run/secrets/corp_ca ]; then \
    install -m 0644 -D /run/secrets/corp_ca /usr/local/share/ca-certificates/corporate.crt; \
    update-ca-certificates; \
  elif [ -s /tmp/certs/SOLUTEC-RootCA_base64.crt ]; then \
    install -m 0644 -D /tmp/certs/SOLUTEC-RootCA_base64.crt /usr/local/share/ca-certificates/corporate.crt; \
    update-ca-certificates; \
  else \
    echo "No corporate CA provided, continuing"; \
  fi'

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci

COPY . .
RUN npm run build -- --configuration production

FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY --from=builder /app/dist/frontend/browser ./
# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
