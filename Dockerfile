FROM openjdk:8-jdk-alpine

EXPOSE 8080

ADD ./target/media-app.jar media-app.jar

# Run the jar file
ENTRYPOINT ["java","-jar","media-app.jar"]

