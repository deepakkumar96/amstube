#Downloading  code updates
FROM alpine/git:1.0.4 as clone
COPY . /app
WORKDIR /app
RUN git pull

#Compiling & Building App
FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY --from=clone /app/amstube /app
RUN mvn package


#Running App
FROM openjdk:8-jdk-alpine
EXPOSE 8080
WORKDIR /app
COPY --from=build /app/target/media-app.jar /app

# Run the jar file 
CMD ["java -jar media-app.jar"]

