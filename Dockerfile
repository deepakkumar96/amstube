#Downloading  code updates

FROM alpine/git:1.0.4

ENTRYPOINT ["git pull"]

#Compiling & Building App

FROM maven:3.5-jdk-8-alpine

ENTRYPOINT ["mvn package"]

FROM openjdk:8-jdk-alpine

EXPOSE 8080

ADD target/media-app.jar media-app.jar

# Run the jar file 
ENTRYPOINT ["java","-jar","media-app.jar"]
