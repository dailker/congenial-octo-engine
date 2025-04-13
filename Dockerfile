# Use a Maven image to build the application
FROM maven:3.8.5-openjdk-21 AS build

WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use a lightweight JDK to run the app
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]