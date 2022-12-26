FROM maven:3.8.4-openjdk-8 AS builder
ADD . .

RUN mvn clean package -DskipTests=true -P pre

From maven:3.8.4-openjdk-8
RUN mkdir /app

WORKDIR /app
COPY --from=builder vhrserver/vhr-web/target/*.jar /app/vhrserver.jar
EXPOSE 8081
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/vhrserver.jar --spring.config.location=/etc/authing/application.yaml "]