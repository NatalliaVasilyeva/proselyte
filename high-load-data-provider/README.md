# high-load-data-provider
Service for create companies and stocks for these companies.

# INFO
### Purpose
The main purpose is to demonstrate how we can use Spring WebFlux to create a simple reactive web application.

### Application work description
This application create companies and stocks by provided scheduled methods. Companies are created once 
after application started. Stocks are created periodically for existing companies that are chosen by random

### Built With

- Java JDK (JDK 17 or higher)
- Maven
- Database (Reactive PostgreSQL)
- Redis
- Docker

### How to run `high-load-data-provider` locally

1. Build postgres image from docker/postgres dir `docker build -t data-provider-db . `
2. Build redis image from /docker/redis dir `docker build -t redis .`
3. Start db from scripts dir `start_db.sh`
4. Start redis from scripts dir `start_redis.sh`
5. Create intellij configuration
6. Run app using intellij (command line is  currently cumbersome)


### Building
Use the following command: `mvn clean install`

It will clean, check for dependencies conflict and finally run integration and unit tests.

To run the tests only, the command is:
`mvn clean test`

## debugging local db
To access the db directly - useful to test queries in the raw data, or fix broken liquibase operations, do the following:
1. get the ID of the Postgresql container: ```$ docker ps```
2. get a shell inside container: ``docker exec -it [DOCKER_CONTAINER_ID] /bin/bash``
3. access the database:
    - for local profile ```psql -U postgres data-provider```
    - for test profile ```psql -U postgres data-provider-test```

## API Endpoints

### Users & Authentication
- POST `/api/v1/register`: Register a new user
- POST `/api/v1/get-token`: Create token for user
- POST `/api/v1/get-api-key`: Create api key for user

### Companies
- GET `/api/v1/companies`: Fetch all companies
- GET `/api/v1/companies/{symbol}`: Fetch company by unique symbol
- POST `/api/v1/company`: Create new company
- POST `/api/v1/companies`: Create new companies


### Stocks
- GET `/api/v1//stocks/{stock_code}`: Fetch all stocks by unique symbol
- GET `/api/v1/stocks/{stock_code}/quote`: Fetch last stock by unique symbol
- POST `/api/v1/stock`: Create new stock
- POST `/api/v1/stocks`: Create new stocks