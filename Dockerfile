#Stage 1: Build with Maven
FROM maven:latest AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests
#CMD ["mvn", "install", "-DskipTests"]

#Stage 2: Run with JDK 21
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/notification-service.jar
CMD ["java", "-jar", "notification-service.jar"]