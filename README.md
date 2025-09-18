
# Interview Case Study

> Staff Automation (QE) Backend - Groupon

- /orders-java/ - Orders Service (Java - Spring Boot) Consumer
- /tax-ruby/ - Tax Service (Ruby - Sinatra) Provider
- /docs/ - Documentation and diagrams

## How to Run Orders Service

### Prerequisites
- Docker
- Docker Compose

### Build and Run with Docker Compose

1. Build the Orders service Docker image (from project root):
    ```bash
    docker-compose build orders
    ```
2. Start Orders service and Redpanda (Kafka-compatible):
    ```bash
    docker-compose up
    ```
3. The Orders API will be available at `http://localhost:8080/orders`

### Example Request
```bash
curl -X POST http://localhost:8080/orders \
    -H 'Content-Type: application/json' \
    -d '{
        "orderId": "123",
        "items": ["item1", "item2"],
        "amount": 100.0,
        "currency": "USD",
        "userId": "user-abc"
    }'
```

### Stopping Services
```bash
docker-compose down
```

### Environment Variables
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka/Redpanda bootstrap servers (default: `redpanda:9092`)
- `TAX_SERVICE_URL`: Base URL for Tax service (default: `http://tax:4567`). The Orders service appends `/tax/calc` when calling it.

---


## Flow Diagram

```mermaid
sequenceDiagram
    participant Client
    participant OrdersService
    participant TaxService
    participant Kafka
    Client->>OrdersService: POST /orders { orderId, items[], amount, currency, userId }
    OrdersService->>TaxService: POST /tax/calc { amount, currency, userId }
    TaxService-->>OrdersService: { taxAmount }
    OrdersService->>Kafka: Publish payments.status.updated { orderId, status, provider, occurredAt }
```

## Technologies

- Java 21 with Spring Boot
- Ruby 3.4.6 with Sinatra
- JUnit and RSpec for unit tests
- Pact for consumer/provider contracts
- Kafka/Redpanda for messaging
- Testcontainers or docker-compose for integration tests
- GitHub Actions for CI/CD

---

## Contract tests (Pact)

Orders (consumer) has Pact tests describing its contract with Tax (provider):

- Happy path: valid request -> 200 with `taxAmount`.
- Non-breaking change: provider returns an extra field the consumer ignores.

> Broker is not required; pact files are written locally to the repo root under `./pacts/`.

### Run consumer Pact tests (orders-java)

Generate pact files using Docker Compose:

```bash
docker compose run --rm orders-contract-tests
```

These pact files will be used later to verify the Ruby provider.
