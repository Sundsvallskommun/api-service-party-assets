# PartyAssets

_The service manages issued permits and other engagements that a stakeholder has towards a municipality_

## Getting Started

### Prerequisites

- **Java 25 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Sundsvallskommun/api-service-party-assets.git
   cd api-service-party-assets
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   - Using Maven:

     ```bash
     mvn spring-boot:run
     ```
   - Using Gradle:

     ```bash
     gradle bootRun
     ```

## Dependencies

This microservice depends on the following services:

- **Party**
  - **Purpose:** Service is used to translate party id to legal id and vice versa
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-party](https://github.com/Sundsvallskommun/api-service-party)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Messaging**
  - **Purpose:** Service is used for sending notification email when data import has not been successful
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-messaging](https://github.com/Sundsvallskommun/api-service-messaging)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

See [API Documentation](#api-documentation) for detailed information on available endpoints.

Alternatively, see the `openapi.yml` file located in directory `src/test/resources/api` for the OpenAPI specification.

### Example Request

```bash
curl -X 'GET' 'http://localhost:8080/2281/assets?assetId=PRH-123456789' -H 'accept: application/json'
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

```yaml
server:
  port: 8080
```

- **Database Settings:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://database-server:port/your_database
    username: your_db_username
    password: your_db_password
```

- **External Service URLs:**

```yaml
integration:
  party:
    url: http://dependency_service_url

spring:
  security:
    oauth2:
      client:
        provider:
          party:
            token-uri: http://token_url
        registration:
          party:
            client-id: some-client-id
            client-secret: some-client-secret

pr3import:
  messaging-integration:
    url: http://dependency_service_url
    oauth2:
      token-uri: http://token_url
      client-id: some-client-id
      client-secret: some-client-secret
      grant-type: client_credentials
```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
config:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-party-assets&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-party-assets)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-party-assets&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-party-assets)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-party-assets&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-party-assets)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-party-assets&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-party-assets)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-party-assets&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-party-assets)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-party-assets&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-party-assets)

## 

&copy; 2024 Sundsvalls kommun
