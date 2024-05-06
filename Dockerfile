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
ENV SERVER_PORT # Enter the port number
ENV SPRING_DATASOURCE_PASSWORD # Enter the password
ENV SPRING_DATASOURCE_USERNAME # Enter the username
ENV SPRING_DATASOURCE_URL # Enter the database URL
ENV SPRING_JPA_HIBERNATE_DDL_AUTO update
ENV SPRING_JPA_DATABASE # Enter the database name
ENV SPRING_JPA_DATABASE_PLATFORM org.hibernate.dialect.PostgreSQLDialect
ENV SPRING_SECURITY_USER_NAME # Enter the username
ENV SPRING_SECURITY_USER_PASSWORD # Enter the password
ENV SPRING_MVC_SERVLET_PATH /
ENV SPRING_KAFKA_BOOTSTRAP_SERVERS # Enter the Kafka server
ENV API_KEY # Enter the API key

# Expose the port the application runs on
EXPOSE # Enter the port number

# Command to run the application
CMD ["java", "-jar", "app.jar"]
