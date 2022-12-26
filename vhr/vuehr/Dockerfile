FROM node:16-alpine as builder

WORKDIR /builder
RUN npm config set registry https://registry.npmmirror.com
RUN npm install -g cnpm --registry=https://registry.npm.taobao.org
COPY package*.json .
RUN cnpm install
COPY . .
RUN npm run build

FROM nginx:alpine
WORKDIR /app
COPY --from=builder /builder/dist .
COPY .docker/eadmin.conf /etc/nginx/conf.d/

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
