# Stage 1: build the jar with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: run on a slim JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/main-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
