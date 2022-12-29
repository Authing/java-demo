FROM node:16-alpine as builder

WORKDIR /builder
RUN npm config set registry https://registry.npmmirror.com && npm install -g cnpm --registry=https://registry.npmmirror.com
COPY . .
RUN cnpm install && npm run build

FROM nginx:alpine
WORKDIR /app
COPY --from=builder /builder/dist .
COPY .docker/vuehr.conf /etc/nginx/conf.d/

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
