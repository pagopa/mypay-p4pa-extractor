# mypay-p4pa-extractor

This application belongs to the **Migration Toolkit** project, which is intended to migrate data from **MyPay4** to **Piattaforma Unitaria** product.

See [p4pa-doc](https://github.com/pagopa/p4pa-doc) for further documentation on Piattaforma Unitaria.

## 🧱 Role

Extract data from MyPay4, MyPivot, FESP and myDictionary sources producing ZIP files to import on Piattaforma Unitaria.

Supports **12 MigrationFileType**:

| # | MigrationFileType | Source | Format |
|---|---|---|---|
| 1 | `ORGANIZATIONS` | MyPay4 | CSV |
| 2 | `ORG_SIL_SERVICES` | MyPay4 | CSV |
| 3 | `DEBT_POSITIONS_TYPE` | MyPay4 | CSV |
| 4 | `DEBT_POSITIONS_TYPE_ORG` | MyPay4 + myDictionary | CSV (enriched) |
| 5 | `DEBT_POSITIONS_TYPE_ORG_OPERATORS` | MyPay4 | CSV |
| 6 | `DEBT_POSITIONS` | MyPay4 | CSV |
| 7 | `DEBT_POSITIONS_PAID` | MyPay4 | XML (RT) |
| 8 | `PAYMENT_NOTIFICATION` | MyPay4 | CSV |
| 9 | `PAYMENTS_REPORTING` | FESP | XML (FdR) |
| 10 | `TREASURY_CSV_COMPLETE` | myPivot4 *(optional)* | CSV (IUF only) |
| 11 | `ASSESSMENTS` | myPivot4 *(optional)* | CSV |
| 12 | `ASSESSMENTS_REGISTRY` | myPivot4 *(optional)* | CSV |

> myDictionary is invoked to populate fields in the `DEBT_POSITIONS_TYPE_ORG` record (`spontaneous_form_code` → `spontaneous_form_structure`).

## 🌐 APIs

See [OpenAPI](openapi/generated.openapi.json), exposed through the following path:
* `/swagger-ui/index.html`

### 📌 Relevant APIs

| Method | Path | Description |
|---|---|---|
| `POST` | `/extract` | Start an extraction for one `MigrationFileType` and one organization |
| `GET` | `/extract/{id}` | Get the extraction status (`RUNNING`, `COMPLETED`, `FAILED`) |
| `GET` | `/extract/{id}/files` | List produced ZIP files by scanning the filesystem (`STORAGE_PATH/<id>/`) |



> `fileTypes` must contain **exactly one element**. Returns `400 Bad Request` if more than one type is provided.


### 📌 Common HTTP status returned

* `202`: Extraction started successfully
* `200`: Successful operation
* `400`: Bad request (e.g. more than one fileType, missing required parameters)
* `401`: Unauthorized (invalid Basic Auth credentials)
* `404`: Extraction not found

## 🔎 Monitoring

See available actuator endpoints through the following path:
* `/actuator`

### 📌 Relevant endpoints

* Health (provide an accessToken to see details): `/actuator/health`
  * Liveness: `/actuator/health/liveness`
  * Readiness: `/actuator/health/readiness`
* Metrics: `/actuator/metrics`
  * Prometheus: `/actuator/prometheus`

Further endpoints are exposed through the JMX console.

## ✏️ Logging

See [log configured pattern](/src/main/resources/logback-spring.xml).

## 🔗 Dependencies

### 🗄️ Resources

* MyPay4 PostgreSQL database (read-only — types 1–8)
* FESP PostgreSQL database (read-only — type 9)
* myPivot4 PostgreSQL database (read-only — types 10–12, **optional**, enabled via `MYPIVOT_ENABLED=true`)

### 🌍 External

* **myDictionary** — REST API to retrieve `spontaneous_form_structure` JSON for `DEBT_POSITIONS_TYPE_ORG` enrichment (`GET /mydictionary/get.html?codice=<spontaneous_form_code>`)

## 🔧 Configuration

See [application.yml](src/main/resources/application.yml) for each configurable property.

### 📌 Relevant configurations

#### 🌐 Application Server

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `SERVER_PORT` | Application server listening port | `8080` |

#### 🔐 Authentication

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `CLIENT_ID` | Username for HTTP Basic Auth on extractor APIs | — |
| `CLIENT_SECRET` | Password for HTTP Basic Auth on extractor APIs | — |

#### 📦 Storage

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `STORAGE_PATH` | Base path where extracted ZIP files are written (`<STORAGE_PATH>/<extractionId>/<fileType>_<date>_partN.zip`) | `/data/extractions` |
| `MULTIPART_MAX_FILE_SIZE` | Max size per ZIP part, aligned to PU `spring.servlet.multipart.max-file-size`. Files exceeding this limit are split into numbered parts | `50MB` |
| `EXPORT_PAGE_SIZE` | Number of records fetched per JDBC page | `1000` |

#### 🗄️ MyPay4 Database

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `MP4_DB_HOST` | MyPay4 PostgreSQL host | — |
| `MP4_DB_PORT` | MyPay4 PostgreSQL port | `5432` |
| `MP4_DB_NAME` | MyPay4 database name | — |
| `MP4_DB_USER` | MyPay4 JDBC user (read-only) | — |
| `MP4_DB_PASSWORD` | MyPay4 JDBC password | — |

#### 🗄️ FESP Database

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `FESP_DB_HOST` | FESP PostgreSQL host | — |
| `FESP_DB_PORT` | FESP PostgreSQL port | `5432` |
| `FESP_DB_NAME` | FESP database name | — |
| `FESP_DB_USER` | FESP JDBC user (read-only) | — |
| `FESP_DB_PASSWORD` | FESP JDBC password | — |

#### 🗄️ myPivot4 Database *(optional)*

| ENV | DESCRIPTION                                                                                      | DEFAULT |
|---|--------------------------------------------------------------------------------------------------|---|
| `MPV4_DB_HOST` | myPivot4 PostgreSQL host *(required if `MYPIVOT_ENABLED=true`)*                                  | — |
| `MPV4_DB_PORT` | myPivot4 PostgreSQL port                                                                         | `5432` |
| `MPV4_DB_NAME` | myPivot4 database name *(required if `MYPIVOT_ENABLED=true`)*                                    | — |
| `MPV4_DB_USER` | myPivot4 JDBC user (read-only) *(required if `MYPIVOT_ENABLED=true`)*                            | — |
| `MPV4_DB_PASSWORD` | myPivot4 JDBC password *(required if `MYPIVOT_ENABLED=true`)*                                    | — |

#### ⚡ Async extraction

| ENV | DESCRIPTION | DEFAULT |
| --- | --- | --- |
| `ASYNC_CORE_POOL_SIZE` | Core thread pool size for async extractions | `2` |
| `ASYNC_MAX_POOL_SIZE` | Max thread pool size for async extractions | `5` |
| `ASYNC_QUEUE_CAPACITY` | Queue capacity for async extraction tasks | `10` |

#### 🌍 External services

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `MYDICTIONARY_BASE_URL` | myDictionary service base URL | — |
| `MYDICT_API_KEY` | myDictionary API key | — |

#### ✏️ Logging

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `LOG_LEVEL_ROOT` | Base level | `INFO` |
| `LOG_LEVEL_PAGOPA` | Base level of custom classes | `INFO` |
| `LOG_LEVEL_SPRING` | Level applied to Spring framework | `INFO` |
| `LOG_LEVEL_SPRING_BOOT_AVAILABILITY` | To print availability events | `DEBUG` |
| `LOGGING_LEVEL_API_REQUEST_EXCEPTION` | Level applied to APIs exception | `INFO` |
| `LOG_LEVEL_PERFORMANCE_LOG` | Level applied to [PerformanceLog](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Logging.pdf) | `INFO` |
| `LOG_LEVEL_PERFORMANCE_LOG_API_REQUEST` | Level applied to [API Performance Log](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Logging.pdf) | `INFO` |
| `LOG_LEVEL_PERFORMANCE_LOG_REST_INVOKE` | Level applied to [REST invoke Performance Log](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Logging.pdf) | `INFO` |

#### 🔁 Integrations

##### 🔗 REST

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `DEFAULT_REST_CONNECTION_POOL_SIZE` | Default connection pool size | `10` |
| `DEFAULT_REST_CONNECTION_POOL_SIZE_PER_ROUTE` | Default connection pool size per route | `5` |
| `DEFAULT_REST_CONNECTION_POOL_TIME_TO_LIVE_MINUTES` | Default connection pool TTL (minutes) | `10` |
| `DEFAULT_REST_TIMEOUT_CONNECT_MILLIS` | Default connection timeout (milliseconds) | `120000` |
| `DEFAULT_REST_TIMEOUT_READ_MILLIS` | Default read timeout (milliseconds) | `120000` |

#### 💼 Business logic

| ENV | DESCRIPTION | DEFAULT |
|---|---|---|
| `STORAGE_PATH` | Base path for extracted ZIP files | `/data/extractions` |
| `MULTIPART_MAX_FILE_SIZE` | Max ZIP part size (aligned to PU upload limit) | `50MB` |
| `EXPORT_PAGE_SIZE` | JDBC fetch page size | `1000` |
| `MYPIVOT_ENABLED` | Enable myPivot4 extraction (types 10/11/12). If `false`, Adapters related to MyPivot are skipped | `false` |
| `BROKER_CF` | Fiscal code of the broker/intermediary running the extraction (used as context metadata) | — |
| `AVG_ROW_SIZE_ORGANIZATIONS` | Average row size in bytes for `ORGANIZATIONS` split calculation (`maxRows = MULTIPART_MAX_FILE_SIZE / avgRowSize`) | `500` |

## 🛠️ Getting Started

### 📝 Prerequisites

Ensure the following tools are installed on your machine:

1. **Java 21+**
2. **Gradle** (or use the Gradle wrapper included in the repository)
3. **Docker** (to build and run on an isolated environment, optional)

Access to the following databases is required:
- MyPay4 PostgreSQL (read-only)
- FESP PostgreSQL (read-only)
- myPivot4 PostgreSQL (read-only, optional — set `MYPIVOT_ENABLED=true`)

### 🔐 Write Locks

```sh
./gradlew dependencies --write-locks
```

### ⚙️ Build

```sh
./gradlew clean build
```

### 🧪 Test

#### 📌 JUnit

```sh
./gradlew test
```

### 🚀 Run local

```sh
./gradlew bootRun
```

### 🐳 Build & run through Docker

```sh
docker build -t mypay-p4pa-extractor .
docker run --env-file <ENV_FILE> mypay-p4pa-extractor
```


### ⚖️ Generate dependencies licenses

```sh
./gradlew generateLicenseReport
```
