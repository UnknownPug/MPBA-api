# This program was created as a bachelor project CTU FEL in Prague

## (CTU - SIT summer semester 2024)

### Authors: Dmitry Rastvorov

### Java version: 17

### Spring Boot version: 3.2.3

### Actual project version: 4.2.7

### ● Main documentation can be found by clicking [here](https://drive.google.com/file/d/1k44jh4FDOH3mNoLpYRbHdH2hwj3EBmG9/view?usp=sharing)

### ● Project also contains Java documentation - [Javadoc](https://unknownpug.github.io/Managing-personal-bank-accounts/)

## Contents

### [Description](#description)

### [Postman](#postman)

### [How To Run](#howtorun)

### [Docker](#docker)

### [Third-party APIs](#api)

### [Video demonstration](#video)

-- -- --

### <a name="description"></a> Description

Nowadays, there are many different banks that try to attract future customers with favorable services.
When creating a card, customers are asked to install the bank's mobile app,
where the user will have full access to their data and accounts.
What many banks don't tell You is that they also have web apps that can also be used to manage their accounts.
Due to this, if many banks have a modern interface of their web application, they are also complex.
The complexity is that it takes time to fully use the application, for example, if You want to make a transaction, You
have to click through several
pages when it could be done in one: change user data, for example, if the location where You live has been changed,
the possibility of transferring funds to another account and more.

The aim of the work is to analyze existing web applications of banks and to develop a server part for managing personal
bank accounts.
The program should present an opportunity to easily and quickly manage bank accounts, change rates,
create new cards, transactions and the ability to change their data.
In the process of the implementation of the project, as well as the bachelor's thesis,
a prototype of the server will be created with functionality covering the needs of the client.
-- -- --

### <a name="postman"></a> Postman

The project also contains a [Postman collection](https://documenter.getpostman.com/view/34110793/2sA3BobY2o)
that can be used to test the server-side application.

-- -- --

### <a name="howtorun"></a> How To Run

To run the project, You need to configure Your workplace:

1) Installed Java 17 and Maven
2) Installed PostgreSQL
3) Installed IDE for running the app (Preferable: IntelliJ IDEA)
4) Installed Postman
5) Installed Git
6) Installed [Kafka](https://kafka.apache.org/quickstart).
   In mine project I was using version 3.1.0, which can be
   downloaded [here](https://archive.apache.org/dist/kafka/3.1.0/kafka_2.13-3.1.0.tgz)
7) Installed Docker (Optional)

If the requirements are met, You need to clone the repository:

1. Clone the repository by `git clone`
2. Open the project in Your IDE

Next, we need to configure the database:
     
     1. Open the database in IntelliJ, click on "New" (or + button)

     2. Choose the datasource and then find and choose the PostgreSQL database
     
     3. Set the port, username and password to Your requirements and click apply

After You have set the database, You need to configure application.yml file before running the project:

1. Open `application.yml` file in `src/main/resources`
2. Set the `port` to the port that You are using for the database (default is `8080`)
3. Set the `username` and `password` to Your database username and password
4. Set the `url` to Your PostgreSQL url
5. Set the security users' `name` and `password` that You are using for the database
6. Set the `kafka.bootstrap-servers` to Your IP and port (example: `localhost:9092`)
7. Set the `api.key` to Your "ExchangeRate-API" key, that You got from the API web page.

After You have configured the `application.yml` file, You need to run the Kafka server by following these steps

1. Open the installed folder of Kafka `cd {path to kafka folder}/bin`
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

After You have run the Kafka, You can run the project

1. Run Maven command: `mvn clean install` (or `mvn clean install -U`)
2. Run the project by executing `Application.java` class
3. Open Postman and enjoy the project

#### Honorable mention:

After you execute the application, you will be able to make a login and logout
of the application because we are using authentication and session cookies to improve application speed.

Also with a login and logout process, status of the user is changing to `STATUS-ONLINE` or `STATUS-OFFLINE`.

To try this feature, You need to follow these steps:

1. After executing the project, You need to open Your browser and enter `http://your_ip:your_port/login` where You will
redirect to the login page. After entering Your valid credentials and pressing Enter, You will be redirected to the page, where You will see something like:

```json
{"type":"about:blank","title":"Not Found","status":404,"detail":"No static resource .","instance":"/"}
```
Your status will be `STATUS_ONLINE`

2. If You try to enter `http://your_ip:your_port/logout`, you will be redirected to the login page and status will be `STATUS_OFFLINE`

-- -- --
### <a name="docker"></a> Docker

This project includes a Dockerfile that can be used to run the project in Docker.

To run the project in Docker, firstly, You need to have Docker installed on Your machine.

Next, You need to configure the Dockerfile:

1. Open the Dockerfile in the root of the project.
2. Set the `SERVER_PORT` to the port that You are using for the database (default is `8080`).
3. Set the `DATASOURCE_USERNAME` and `DATASOURCE_PASSWORD` to Your database username and password.
4. Set the `DATASOURCE_URL` to Your PostgreSQL url.
5. Set the JPA DATABASE `username` and `password` that You are using for the database.
6. Set the security username and password that You are using for the database.
7. Set the `SPRING_KAFKA_BOOTSTRAP_SERVERS` to Your IP and port (e.g., Kafka server) (example: `localhost:9092`).
8. Set the `API_KEY` to Your "ExchangeRate-API" key, that You got from the API web page.
9. Set the `EXPOSE` port to the port that You are using for the database.

After the Docker is configured, You need to build the project by running this line in the console 
```docker
docker build -t name-image .
```
Where `name-image` is the name of the image that You want to give to the project.

You can change the name of the image to Your needs.

-- -- --
### <a name="api"></a>Third-party APIs

Also in this application was used third-party APIs

1. [Exchange Rate API](https://app.exchangerate-api.com/dashboard) for getting exchange rate currencies
2. [REST Countries API](https://restcountries.com) for validation of existing countries
-- -- --
### <a name="video"></a> Video demonstration

Also attached to the description is a video demonstration of the project, where you can see the main functions of the application:


https://github.com/UnknownPug/Managing-personal-bank-accounts/assets/73190129/bce71ae2-070d-4547-a2df-c49abc2edc4c



-- -- --
## Thank You for Your attention!
