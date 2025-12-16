# Data Ingestion & Aggregation Service

Event-driven data ingestion and aggregation service using Spring Boot, Kafka, and PostgreSQL.

## Features

- **Event Ingestion**: REST API for single and bulk event ingestion
- **Kafka Integration**: Event-driven architecture with Kafka
- **Idempotency**: Duplicate event detection at multiple layers
- **Aggregation**: Time-based event aggregation (minute/hour buckets)
- **Dead Letter Queue**: Failed event handling and retry
- **Monitoring**: Actuator endpoints with health checks and metrics

## 🛠️ Technology Stack

| Component | Version/Detail | Notes |
| :--- | :--- | :--- |
| **Language** | **Java 21** | Latest Long-Term Support (LTS) release. |
| **Framework** | **Spring Boot 4.0.0** | Next-generation Spring Boot. |
| **ORM** | Spring Data JPA (Hibernate 7.x) | Persistence layer. |
| **Messaging** | Apache Kafka 3.6+ | High-throughput event streaming. |
| **Database** | PostgreSQL 17+ | Chosen for reliable JSONB support. |
| **Schema Management** | Flyway | For versioned and controlled database migrations. |
| **API Docs** | Springdoc OpenAPI (Swagger UI) | Interactive API documentation. |

## 🚀 Prerequisites

Ensure the following tools are installed on your system:

* **JDK 21** or higher
* **Maven 3.8+**
* **Docker & Docker Compose** (for running the infrastructure)
* **Git**

## Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd data-ingestion-service
```

### 2. Start Infrastructure
```
docker compose up -d
```

This will start:
- PostgreSQL (port 5432)
- Apache Kafka (port 9092)
- Kafka UI (port 8090)

### 3. Build Application
```bash
mvn clean install
```

### 4. Run Application
```bash
mvn spring-boot:run
```

Or with specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Application will start on **http://localhost:8080**

### 5. Verify Setup

Check health:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "kafka": {"status": "UP"}
  }
}
```

## API Documentation

### Ingest Single Event

**POST** `/events`
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "event-123",
    "tenantId": "tenant-1",
    "source": "web",
    "eventType": "click",
    "timestamp": "2024-12-16T10:00:00Z",
    "payload": {
      "page": "home",
      "userId": "user-456"
    }
  }'
```

Response:
```json
{
  "eventId": "event-123",
  "status": "ACCEPTED",
  "message": "Event accepted",
  "timestamp": "2024-12-16T10:00:01Z"
}
```

### Ingest Bulk Events

**POST** `/events/bulk`
```bash
curl -X POST http://localhost:8080/events/bulk \
  -H "Content-Type: application/json" \
  -d '[
    {
      "eventId": "event-201",
      "tenantId": "tenant-1",
      "source": "mobile",
      "eventType": "view",
      "timestamp": "2024-12-16T10:05:00Z",
      "payload": {"screen": "dashboard"}
    },
    {
      "eventId": "event-202",
      "tenantId": "tenant-1",
      "source": "mobile",
      "eventType": "click",
      "timestamp": "2024-12-16T10:06:00Z",
      "payload": {"button": "submit"}
    }
  ]'
```

### Query Events

**GET** `/events?tenantId={tenantId}&from={timestamp}&to={timestamp}&page=0&size=20`
```bash
curl "http://localhost:8080/events?tenantId=tenant-1&page=0&size=10"
```

### Query Metrics

**GET** `/metrics?tenantId={tenantId}&bucketSize={MINUTE|HOUR}&from={timestamp}&to={timestamp}`
```bash
curl "http://localhost:8080/metrics?tenantId=tenant-1&bucketSize=HOUR&from=2024-12-16T00:00:00Z&to=2024-12-16T23:59:59Z"
```

## Project Structure