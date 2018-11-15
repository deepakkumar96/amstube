#Downloading  code updates

FROM alpine/git:1.0.4

COPY . /app

WORKDIR /app

CMD ["git pull"]

#Compiling & Building App

FROM maven:3.5-jdk-8-alpine

CMD ["mvn package"]

FROM openjdk:8-jdk-alpine

EXPOSE 8080

ADD target/media-app.jar media-app.jar

# Run the jar file 
ENTRYPOINT ["java","-jar","media-app.jar"]

