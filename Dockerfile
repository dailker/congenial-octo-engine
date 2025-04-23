#
# Build stage
#
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml and resolve dependencies first (cache benefit)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy actual source files
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/Tasky-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (change if your app uses a different one)
EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-cp", "app.jar"]
