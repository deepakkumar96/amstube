FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY . /app
EXPOSE 8080

ADD /app/target/media-app.jar /app

# Run the jar file
ENTRYPOINT ["java","-jar","media-app.jar"]

