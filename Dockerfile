# Use Maven with Java 21 to build the app
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use OpenJDK 21 to run the app
FROM eclipse-temurin:21-jdk

WORKDIR /

COPY --from=build /target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
