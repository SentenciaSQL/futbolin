FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npm run build \
    && mkdir -p /out \
    && APP_DIR="$(dirname "$(find dist -name index.html | head -1)")" \
    && test -n "$APP_DIR" \
    && cp -R "$APP_DIR"/. /out/

FROM nginx:1.27-alpine
COPY --from=build /out /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
