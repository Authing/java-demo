FROM node:16-alpine as builder

WORKDIR /builder
RUN npm config set registry https://registry.npmmirror.com
COPY . .
RUN npm install && npm run build

FROM nginx:alpine
WORKDIR /app
COPY --from=builder /builder/dist .
COPY .docker/music-client.conf /etc/nginx/conf.d/

EXPOSE 8082

CMD ["nginx", "-g", "daemon off;"]
