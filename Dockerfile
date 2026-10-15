# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn -B -DskipTests clean package

# Stage 2: Deploy
FROM eclipse-temurin:21-jre
COPY --from=build /target/deulaubaba-0.0.1-SNAPSHOT.jar /deulaubaba.jar
LABEL authors="niallantony"
EXPOSE 8080

ENTRYPOINT java -jar /deulaubaba.jar --server.port=$PORT