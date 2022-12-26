FROM node:16-alpine as builder

WORKDIR /builder
RUN npm config set registry https://registry.npmmirror.com
COPY package*.json .
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
WORKDIR /app
COPY --from=builder /builder/dist .
COPY .docker/eadmin.conf /etc/nginx/conf.d/

EXPOSE 8082

CMD ["nginx", "-g", "daemon off;"]
