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
- **Relation**
  - **Purpose:** Service is used for relating a created asset to the resource it originates from (`sourceReference`)
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-relation](https://github.com/Sundsvallskommun/api-service-relation)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

See [API Documentation](#api-documentation) for detailed information on available endpoints.

Alternatively, see the `openapi.yaml` file located in directory `src/integration-test/resources/api` for the OpenAPI
specification.

### Example Request

```bash
curl -X 'GET' 'http://localhost:8080/2281/assets?assetId=PRH-123456789' -H 'accept: application/json'
```

### Searching assets

`GET /{municipalityId}/assets/search` returns a page of the assets in a municipality, whatever their status. Unlike
`GET /assets` it needs no `partyId`, and it includes drafts; leave them out with `status ! 'DRAFT'`. Which assets come
back is decided by `filter`, written in the part of the
[SpringFilter](https://github.com/turkraft/spring-filter/blob/85730f950a5f8623159cc0eb4d737555f9382bb7/README.md#syntax)
syntax described below. Without one, every asset in the municipality is returned.

```bash
curl -G 'http://localhost:8080/2281/assets/search' \
  --data-urlencode "filter=type:'AlcoholServingPermit' and status:'ACTIVE' and validTo < '2027-01-01'" \
  --data-urlencode 'sortBy=validTo' --data-urlencode 'sortDirection=DESC' \
  --data-urlencode 'page=1' --data-urlencode 'limit=50'
```

The filter refers to the attributes by the names they have in the asset model of the API:

|                                                  Attributes                                                  |                                       Example                                       |
|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `id`, `assetId`, `origin`, `partyId`, `type`, `status`, `statusReason`, `description`, `title`, `replacesId` | `type:'AlcoholServingPermit' and status in ['ACTIVE', 'TEMPORARY']`                 |
| `issued`, `validTo`                                                                                          | `validTo < '2027-01-01' or validTo is null`                                         |
| `created`, `updated`                                                                                         | `updated > '2026-11-01T00:00:00+01:00'`                                             |
| `additionalParameters`, `additionalParameters.key`, `additionalParameters.value`                             | `exists(additionalParameters.key:'errandId' and additionalParameters.value:'1234')` |
| `jsonParameters`, `jsonParameters.key`, `jsonParameters.schemaId`, `jsonParameters.value`                    | `jsonParameters.schemaId:'2281_aot_alcohol_party_asset_0.1'`                        |

Each condition has the attribute on the left and, for the operators that take one, a quoted value or a list of quoted
values on the right. Conditions are combined with `and`, `or` and `not`, and grouped with parentheses:

|                               Condition                               |                Applies to                |                      Example                      |
|-----------------------------------------------------------------------|------------------------------------------|---------------------------------------------------|
| `:` (equal), `!` (not equal), `>`, `>:`, `<`, `<:`, `between … and …` | attributes that hold a single value      | `validTo between '2026-01-01' and '2026-12-31'`   |
| `in`, `not in` with a list                                            | attributes that hold a single value      | `status in ['ACTIVE', 'TEMPORARY']`               |
| `is null`, `is not null`                                              | attributes that hold a single value      | `validTo is null`                                 |
| `~`, or `~~` to ignore case, with one pattern                         | text attributes                          | `description ~~ 'park*'`                          |
| `is empty`, `is not empty`                                            | `additionalParameters`, `jsonParameters` | `jsonParameters is not empty`                     |
| `exists(…)` around one condition                                      | conditions                               | `not exists(additionalParameters.key:'errandId')` |

The attributes that hold a single value are all but `additionalParameters` and `jsonParameters`, and the text attributes
are those of them other than `issued`, `validTo`, `created`, `updated` and `status`. In a pattern, `*` stands for any
text, and a pattern without `*` matches text that contains it. The rest of the SpringFilter syntax, such as `xor`,
functions other than `exists` and parentheses around an attribute or a value, is not supported.

Dates are written as `2027-01-01`, and `created` and `updated` with time and offset as `2026-11-01T00:00:00+01:00`,
whatever the language of the request. The same goes for `issued` and `validTo` as parameters of `GET /assets`.

Conditions that must hold for the same parameter go inside `exists(...)`, so the example above finds the assets whose
`errandId` is `1234`, not those that have an `errandId` and some other parameter with the value `1234`. Likewise,
`not exists(additionalParameters.key:'errandId')` finds the assets without an `errandId`, whereas `not` directly in front
of a condition on a parameter only asks for some parameter that does not meet it.

The answer is `400 Bad Request` for a filter that cannot be parsed, that refers to any other attribute, that is not made
of the conditions above, that uses an operator on an attribute it does not apply to, such as `issued ~ '2025*'`, or that
compares an attribute with a value it cannot hold, such as a status that does not exist, an empty value or a date in
another format. The response names each problem, and lists the attributes that can be filtered on when one is unknown.

Paging starts at `page` 1, and `limit` is 100 assets per page unless given, at most 1000. `sortBy` takes one or more of
the attributes above that hold a single value, and `sortDirection` (`ASC` unless given) applies to all of them. Without
`sortBy` the assets are sorted by `created`. `id` is always added as the last sort property, in the same direction, so
that assets with equal values keep the same order from one page to the next. The response holds the assets and the
paging details:

```json
{
  "assets": [ … ],
  "_meta": { "page": 1, "limit": 50, "count": 50, "totalRecords": 120, "totalPages": 3, "sortBy": ["validTo", "id"], "sortDirection": "DESC" }
}
```

Conditions on `partyId`, `assetId`, `status` (alone or with `validTo`) and on the key and value of an additional
parameter are backed by indexes, as is sorting by `created`. A `~` pattern that starts with a wildcard, and any condition
on `jsonParameters.value`, has to read every asset in the municipality.

### Title

`title` is optional display text for the citizen, such as "Stadigvarande tillstånd för servering av alkohol" on Mina
sidor. It is at most 255 characters. It can be set when an asset is created and changed while the asset is a draft,
active or temporary. A blank title is stored as no title, so sending `"title": ""` removes it. It is not a parameter of
`GET /assets`, but the [search](#searching-assets) can filter on it.

### Changing a live asset

`PATCH /assets/{id}` changes an asset that is `ACTIVE` or `TEMPORARY` in place. Besides `status` and `statusReason` it
takes `title`, `validTo`, `indefinitely`, `additionalParameters` and `jsonParameters`, with the same rules as on a
draft. A field left out stays as it is. `additionalParameters` replaces the whole map and `jsonParameters` the whole
list, so send every key and parameter the asset should keep, not only the one that changed. Each change records the
previous state as a revision (see below). Once an asset is blocked, has expired, or has been replaced or revoked, only
its status can change, and a request carrying any of those fields gets `400 Bad Request`.

To change the person responsible for serving on a permit that also has serving hours, read the asset, change that one
key and send the map back whole. Leaving out `serveringstid` here would remove it.

```bash
curl -X 'PATCH' 'http://localhost:8080/2281/assets/{id}' \
  -H 'If-Match: "3"' -H 'Content-Type: application/json' \
  -d '{"additionalParameters":{"serveringsansvarig":"Anna Andersson","serveringstid":"11-01"}}'
```

Use this for changes to the same permit, such as a new person responsible for serving or longer opening hours after a
decision. When the permit is replaced by a new one, copy it instead with `POST /assets/{id}`. The copy is a draft with
its own id, a `replacesId` pointing back, and every attachment. Activating it marks the original `REPLACED`.

When the permit is revoked, set its status to `REVOKED`. Its content and attachments are then locked, the nightly job
that expires permits leaves it alone, and it cannot be copied. It can be made `ACTIVE` again, for instance when a court
overturns the revocation. A `statusReason` is accepted only when the municipality has registered reasons for `REVOKED`
under `/{municipalityId}/metadata/statusreasons`, and is then required.

```bash
curl -X 'PATCH' 'http://localhost:8080/2281/assets/{id}' \
  -H 'Content-Type: application/json' \
  -d '{"status":"REVOKED"}'
```

### Attachments

An asset can carry the files that belong to the permit itself — the drawing of the premises where alcohol may be served,
for instance. They are stored with the asset rather than beside it, since the permit is not valid without them, and they
follow the asset through its life: a draft made from an active asset gets its own copy of every file, and deleting an
asset deletes them.

Uploads are `multipart/form-data` with the file in the `attachment` part, at most 50 MB per file. Any file type is
accepted: callers are other services that already decide which types they allow, and a second list here would only
drift out of step with theirs. The `Content-Type` the client declares for the part is stored and sent back on download.
One that is missing, cannot be parsed, is a wildcard such as `*/*`, or is longer than 255 characters is stored as
`application/octet-stream` instead. Files can be added, changed and removed
while the asset is `DRAFT`, `ACTIVE` or `TEMPORARY`; once it is blocked, has expired, or has been replaced or revoked,
its attachments can still be listed and downloaded but no longer changed.

```bash
curl -X 'POST' 'http://localhost:8080/2281/assets/{id}/attachments' \
  -F 'attachment=@lokalritning.pdf;type=application/pdf' \
  -F 'category=LOKALRITNING'
```

Removing a file marks it rather than erasing it. It disappears from the listing and can no longer be changed or removed
again, but it stays downloadable at its own URL, because an earlier revision of the permit still refers to it and that
history would otherwise point at a file that no longer exists. Deleting the whole asset does erase them.

To swap one file for a newer version, such as a reissued tillståndsbevis, upload the new file with a `replaces` part
naming the old attachment. The new file gets its own id. The old one is removed the same way a delete removes it, and
both happen in one revision, so the permit never carries both files. The new attachment takes the old one's
`category` and `description` unless the request sets them.

```bash
curl -X 'POST' 'http://localhost:8080/2281/assets/{id}/attachments' \
  -F 'attachment=@tillstandsbevis.pdf;type=application/pdf' \
  -F 'replaces={attachmentId}'
```

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

Each revision records who created it, taken from the `X-Sent-By` header. A draft carries whoever changed it last, and
revision 0 of an activated draft carries whoever activated it. That header needs both a value and a type to be
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
  relation:
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
          relation:
            token-uri: http://token_url
        registration:
          json-schema:
            client-id: some-client-id
            client-secret: some-client-secret
          party:
            client-id: some-client-id
            client-secret: some-client-secret
          relation:
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
