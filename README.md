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
- **JSON Schema**
  - **Purpose:** Service is used for validating JSON structures against schemas
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-json-schema](https://github.com/Sundsvallskommun/api-service-json-schema)
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

### Attachments

An asset can carry the files that belong to the permit itself — the drawing of the premises where alcohol may be served,
for instance. They are stored with the asset rather than beside it, since the permit is not valid without them, and they
follow the asset through its life: a draft made from an active asset gets its own copy of every file, and deleting an
asset deletes them.

Uploads are `multipart/form-data` with the file in the `attachment` part, at most 50 MB per file. Allowed types are PDF,
PNG, JPEG, TIFF and Word (`.docx`), checked against the `Content-Type` the client declares for the part — so the type
has to be spelled out for anything curl does not recognise from the extension. Files can be added, changed and removed
while the asset is `DRAFT`, `ACTIVE` or `TEMPORARY`; once it is blocked, has expired or been replaced, its attachments
can still be listed and downloaded but no longer changed.

```bash
curl -X 'POST' 'http://localhost:8080/2281/assets/{id}/attachments' \
  -F 'attachment=@lokalritning.pdf;type=application/pdf' \
  -F 'category=LOKALRITNING'
```

Removing a file marks it rather than erasing it. It disappears from the listing and can no longer be changed or removed
again, but it stays downloadable at its own URL, because an earlier revision of the permit still refers to it and that
history would otherwise point at a file that no longer exists. Deleting the whole asset does erase them.

### Revision history

A change to a live asset first writes a snapshot of what it looked like beforehand. The asset row itself is the newest
revision, and it is returned alongside the older ones by two endpoints:

```bash
curl -X 'GET' 'http://localhost:8080/2281/assets/{id}/revisions'
curl -X 'GET' 'http://localhost:8080/2281/assets/{id}/revisions/0'
```

The listing returns the newest revision first, and its first entry is always the asset's current state, so a permit that
has not changed since it went live still returns one revision. Numbering starts at 0, the asset as it entered service,
whether it was created outright or activated from a draft. The number moves only when a snapshot is taken, so the
sequence has no gaps.

Nothing that happens while an asset is `DRAFT` is recorded. Not its fields, not its attachments, and not the moment it
goes active. Activating a draft sets the asset to revision 0 and writes no snapshot, so a permit assembled as a draft
with ten drawings attached one at a time goes live at revision 0 rather than dragging a snapshot behind it for every
file. A draft is not a permit yet, and how it was put together is not part of the permit's history. The first change
after activation writes revision 0 into the history and moves the asset to revision 1.

Each revision records who created it, taken from the `X-Sent-By` header. That header needs both a value and a type to be
read at all, so `X-Sent-By: joe01doe` is silently ignored while `X-Sent-By: joe01doe; type=adAccount` is not. Requests
without the header, and the nightly job that expires permits, leave the actor empty.

Two writes that overlap in time are rejected rather than merged: the one that loses gets `409 Conflict` and has to
reload. That covers attachment changes too, since adding, renaming or removing a file is a change to the permit. What it
does not cover is reading a permit, waiting, and writing after someone else already has. That write succeeds and the
earlier change is overwritten, though both are visible afterwards in the revision history.

To close that gap, read the permit first and send the `ETag` you got back as `If-Match`:

```bash
curl -i -X 'GET' 'http://localhost:8080/2281/assets/{id}'          # ETag: "3"
curl -X 'PATCH' 'http://localhost:8080/2281/assets/{id}' \
  -H 'If-Match: "3"' -H 'Content-Type: application/json' \
  -d '{"status":"BLOCKED"}'
```

The header is optional, and leaving it out behaves exactly as before. Sending it means the update is rejected with
`412 Precondition Failed` if the permit changed after the read the tag came from. Only a plain entity-tag is understood,
so `*`, weak tags and lists of tags all count as a mismatch. The tag is not the revision number. It is a separate
counter that moves on every write to the permit, so editing a draft or renaming an attachment both invalidate a tag read
before them, even where no revision was recorded.

Deleting an asset deletes its history with it, so nothing is readable afterwards through any endpoint.

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

- **Scheduler:**

```yaml
scheduler:
  asset-expiration:
    cron: '0 0 0 * * *'       # When to run (default: midnight every day)
    lock-at-most-for: 'PT1H'  # Max distributed lock duration
```

- **External Service URLs:**

```yaml
integration:
  json-schema:
    url: http://dependency_service_url
  party:
    url: http://dependency_service_url

spring:
  security:
    oauth2:
      client:
        provider:
          json-schema:
            token-uri: http://token_url
          party:
            token-uri: http://token_url
        registration:
          json-schema:
            client-id: some-client-id
            client-secret: some-client-secret
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
