# OrderFlow

OrderFlow is a production-oriented Order Management System built with Java 17 and Spring Boot.

The project demonstrates REST API design, transactional order processing, PostgreSQL persistence, event-driven architecture with Apache Kafka, and the Transactional Outbox Pattern for reliable event publication.

## Architecture

```text
                        Client
                          |
                          v
                    Order REST API
                          |
                          v
                     OrderService
                          |
                 @Transactional
                 /            \
                v              v
             orders       outbox_events
                \              /
                 \ PostgreSQL /
                       |
                       v
                Outbox Publisher
                       |
                       v
                 Apache Kafka
                       |
                 order-events
                       |
                       v
                Audit Consumer
```

## Technology Stack

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL 16
- Apache Kafka 3.9
- Spring Kafka
- Jakarta Bean Validation
- Maven
- Docker

## Features

### Order API

OrderFlow provides REST APIs for creating and retrieving orders.

Supported order attributes include:

- Instrument
- BUY / SELL side
- Quantity
- MARKET / LIMIT order type
- Price
- Order status

### Order Lifecycle

Orders follow controlled state transitions:

```text
RECEIVED
   |
   +----> VALIDATED ----> ACCEPTED
   |
   +----> REJECTED
   |
   +----> CANCELLED
```

Invalid lifecycle transitions return HTTP `409 Conflict`.

### PostgreSQL Persistence

Orders are persisted using Spring Data JPA and PostgreSQL.

Transaction boundaries are managed with Spring's `@Transactional` support.

### Event-Driven Architecture

Creating an order generates an `OrderCreatedEvent`.

Events contain:

- eventId
- orderId
- instrumentId
- side
- quantity
- orderType
- price
- occurredAt

Kafka messages use the order ID as the message key.

### Transactional Outbox Pattern

OrderFlow avoids directly performing a database write and Kafka publication as two independent operations.

Instead, the order and its corresponding outbox event are stored in PostgreSQL within the same database transaction.

```text
BEGIN TRANSACTION

INSERT orders
INSERT outbox_events (PENDING)

COMMIT
```

A scheduled outbox publisher retrieves pending events and publishes them to Kafka.

After Kafka acknowledges the event, the outbox record is marked:

```text
PUBLISHED
```

This reduces the risk of losing events when failures occur between database persistence and message publication.

### Kafka Consumer

An audit consumer subscribes to the `order-events` topic using the:

```text
orderflow-audit
```

consumer group.

The consumer receives and deserializes `OrderCreatedEvent` messages.

## REST Endpoints

### Create Order

```http
POST /api/orders
```

Example:

```json
{
  "instrumentId": "NVDA",
  "side": "BUY",
  "quantity": 75,
  "orderType": "LIMIT",
  "price": 195.25
}
```

### Get Order

```http
GET /api/orders/{id}
```

### Get All Orders

```http
GET /api/orders
```

### Validate Order

```http
POST /api/orders/{id}/validate
```

### Accept Order

```http
POST /api/orders/{id}/accept
```

### Reject Order

```http
POST /api/orders/{id}/reject
```

### Cancel Order

```http
POST /api/orders/{id}/cancel
```

## Running Locally

### Requirements

- Java 17
- Maven
- Docker

### Start Infrastructure

```bash
docker compose up -d
```

### Run OrderFlow

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

### Create an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "instrumentId": "NVDA",
    "side": "BUY",
    "quantity": 75,
    "orderType": "LIMIT",
    "price": 195.25
  }'
```

## Reliability Model

The current implementation uses the Transactional Outbox Pattern and provides an at-least-once event publication model.

Because an application failure can occur after Kafka acknowledges a message but before the outbox transaction marks it as published, duplicate event delivery remains possible.

Consumer idempotency and additional retry/DLT handling are planned improvements.

## Roadmap

- Idempotent Kafka consumers
- Retry policies and Dead Letter Topics
- Explicit database migrations with Flyway
- Redis caching and idempotency support
- Asynchronous pricing service
- Python/FastAPI pricing integration
- Resilience4j
- Integration testing with Testcontainers
- Metrics with Micrometer and Prometheus
- Grafana dashboards
- Distributed tracing / correlation IDs
- Load testing and measured performance benchmarks
- CI/CD pipeline

## Project Status

OrderFlow is under active development.

The current version implements the core order lifecycle, PostgreSQL persistence, Kafka event publication/consumption, and a transactional outbox workflow.