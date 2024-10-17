# Backend API for managing personal banking accounts

## (CTU - SIT winter semester 2025)

### Authors: Dmitry Rastvorov

### Java version: 21

### Spring Boot version: 3.2.4

### ● Project documentation can be found by clicking [here]().

### ● Project also contains a user manual which can be found by clicking [here](https://unknownpug.github.io/MPBA-api/).

## Contents

### [Installation process](#installation)

#### [1. Setting requirements](#requirements)

#### [2. Installation process](#installation)

#### [3. Database configuration](#database)

#### [4. Application configuration](#configuration)

#### [5. Execution of the Kafka](#kafka)

#### [6. Executing the project](#execution)

### [Third-party APIs](#api)

-- -- --

### <a name="installation"></a> Installation Process

#### <a name="requirements"></a>1. Setting requirements

Before running the project, You need to have the following requirements:

1. Installed Java 21 and Maven 4.0.0.
2. Installed PostgreSQL.
3. Installed IDE for running the app (Preferable: IntelliJ IDEA).
4. Installed Postman or web version.
5. Installed Git.
6. Installed [Kafka 3.1.0](https://archive.apache.org/dist/kafka/3.1.0/kafka_2.13-3.1.0.tgz).

#### <a name="installation"></a>2. Installation process

After You have installed all the requirements, You can start the installation process:

1. Copy an SSH link from the repository.
2. Open terminal and execute `git clone git@github.com:UnknownPug/MPBA-api.git`.
3. Open the project folder in Your IDE.

#### <a name="database"></a>3. Database configuration

Next, You need to set up the database:

     1. Open the database in IntelliJ, click on "+" button.

     2. Choose the datasource and then find and choose the PostgreSQL database.
     
     3. Set the port (if port 5432 is already in use), username and password 
     to Your requirements and click "Apply".

#### <a name="configuration"></a>4. Application Configuration

After you have set up the database, open the `src/main/resources/application.yml` file and review the configuration.
If it does not meet your requirements, modify it accordingly:

1. Set the `server.port` to `8082`, or to any other desired port for your web services.

2. Update the `spring.datasource.username` and `spring.datasource.password` with the credentials you configured in your
   PostgreSQL database.

3. Specify the `spring.datasource.url` as `jdbc:postgresql://localhost:5432/postgres` or as appropriate for your
   database setup.

4. Configure the security settings by setting `spring.security.user.name` and `spring.security.user.password` to the
   appropriate values (default: `postgres`).

5. Set `spring.kafka.bootstrap-servers` to your server's IP address and port (default: `localhost:9092`).

6. Enter the generated API key from the ExchangeRate-API in the `api.key` field.
   
7. For JWT security, set the `security.jwt.secret-key` to your desired secret and configure
   `security.jwt.expiration-time` as needed (default: `3600000` milliseconds).

#### <a name="kafka"></a>5. Execution of the Kafka

To run the Kafka, You need to follow these steps:

1. Open the installed folder of Kafka `cd {path to kafka folder}/bin`.
2. Run Zookeeper:

```shell
zookeeper-server-start ../config/zookeeper.properties 
```

3. Run Kafka server:

```shell
 kafka-server-start ../config/server.properties
```

4. Run the Kafka console for reading events:

```shell
kafka-console-consumer --topic messages --from-beginning --bootstrap-server Your_ip:Your_port
```

#### <a name="execution"></a>6. Executing the project

To run the project, follow these steps:

1. Run Maven command: `mvn clean install` (or `mvn clean install -U`).

2. Run the project by executing `Application.java` class.

-- -- --

### <a name="api"></a>Third-party APIs

Also in this application was used third-party APIs:

1. [Exchange Rate API](https://app.exchangerate-api.com/dashboard) for getting exchange rate currencies
2. [REST Countries API](https://restcountries.com) for validation of existing countries
