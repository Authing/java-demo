FROM node:16-alpine as builder

WORKDIR /builder
RUN npm config set registry https://registry.npmmirror.com
COPY . .
RUN yarn && yarn build

FROM nginx:alpine
WORKDIR /app
COPY --from=builder /builder/dist .
COPY .docker/music-manage.conf /etc/nginx/conf.d/

EXPOSE 8083

CMD ["nginx", "-g", "daemon off;"]
