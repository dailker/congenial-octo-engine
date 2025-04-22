#
# Build stage
#
FROM eclipse-temurin:21-jdk as build
RUN apt-get update && apt-get install -y maven
COPY . .
RUN mvn package -Pproduction -DskipTests

#
# Package stage
#
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /target/demo-0.0.1-SNAPSHOT.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "demo.jar"]
