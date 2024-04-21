# Stage 1: Build the application
FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /tmp/dockerapp
COPY pom.xml .
COPY src ./src/
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Create the Docker image
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /tmp/dockerapp/target/*.jar /app/app.jar

# Set environment variables
ENV SERVER_PORT 8082
ENV SPRING_DATASOURCE_PASSWORD postgres
ENV SPRING_DATASOURCE_USERNAME postgres
ENV SPRING_DATASOURCE_URL jdbc:postgresql://host.docker.internal:5432/postgres
ENV SPRING_JPA_HIBERNATE_DDL_AUTO update
ENV SPRING_JPA_DATABASE postgresql
ENV SPRING_JPA_DATABASE_PLATFORM org.hibernate.dialect.PostgreSQLDialect
ENV SPRING_SECURITY_USER_NAME postgres
ENV SPRING_SECURITY_USER_PASSWORD postgres
ENV SPRING_MVC_SERVLET_PATH /
ENV SPRING_KAFKA_BOOTSTRAP_SERVERS localhost:9092
ENV API_KEY 2d03edf7aa4c0318731708ae

# Expose the port the application runs on
EXPOSE 8082

# Command to run the application
CMD ["java", "-jar", "app.jar"]
