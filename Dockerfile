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

# Set environment variable  (same as for application.yml file):

# Enter the web server port number
ENV SERVER_PORT

# Enter the datasource password 
ENV SPRING_DATASOURCE_PASSWORD

# Enter the datasource username
ENV SPRING_DATASOURCE_USERNAME

# Enter the database URL
ENV SPRING_DATASOURCE_URL

ENV SPRING_JPA_HIBERNATE_DDL_AUTO update
ENV SPRING_JPA_DATABASE postgresql
ENV SPRING_JPA_DATABASE_PLATFORM org.hibernate.dialect.PostgreSQLDialect

# Enter the username
ENV SPRING_SECURITY_USER_NAME

# Enter the password
ENV SPRING_SECURITY_USER_PASSWORD

ENV SPRING_MVC_SERVLET_PATH /

# Enter the Kafka bootstrap server ip and port
ENV SPRING_KAFKA_BOOTSTRAP_SERVERS

# Enter the API key from ExchangeRate-API
ENV API_KEY

# Expose the port the application runs on

# Enter the web server port number
EXPOSE 

# Command to run the application
CMD ["java", "-jar", "app.jar"]
