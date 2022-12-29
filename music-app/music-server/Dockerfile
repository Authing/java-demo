FROM maven:3.8.4-openjdk-8 AS builder
COPY settings.xml /usr/share/maven/conf/settings.xml
ADD . .

RUN mvn clean package -DskipTests=true -P pre

From maven:3.8.4-openjdk-8
RUN mkdir /app

WORKDIR /app
COPY --from=builder target/*.jar /app/music-server.jar
EXPOSE 8888
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/music-server.jar --spring.config.location=/etc/authing/application.properties"]
