# Build the application
FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Create the runtime image
FROM openjdk:21-jdk-slim
WORKDIR /app
ARG JAR_FILE=target/freight-api.jar
COPY --from=build /app/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]