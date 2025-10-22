FROM node:20-alpine
WORKDIR /app
COPY package.json ./
RUN npm config set strict-ssl false && npm install --omit=dev
COPY index.js ./
EXPOSE 3000
CMD ["npm","start"]