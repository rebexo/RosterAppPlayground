# 1. Build Stage
FROM node:20-alpine AS build-stage
WORKDIR /app

# Kopiere package.json und lock-files
COPY package*.json ./
# Installiere Abhängigkeiten sauber
RUN npm ci

# Kopiere den Rest und baue
COPY . .
# 'npm run build' führt laut deiner package.json auch den Type-Check durch
RUN npm run build

# 2. Production Stage (Nginx)
FROM nginx:stable-alpine
# Kopiere den Build-Ordner 'dist' (Standard bei Vite)
COPY --from=build-stage /app/dist /usr/share/nginx/html
# Kopiere unsere Nginx-Config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]