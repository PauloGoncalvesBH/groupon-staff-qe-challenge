# Orders Service (Java)

Minimal Spring Boot REST API for Orders, as required by the Groupon Staff QE challenge.

## Features
- POST /orders endpoint: accepts `{ orderId, items[], amount, currency, userId }`
- Calls external Tax service (stubbed for now)
- Publishes `payments.status.updated` event to Kafka/Redpanda
- Fallback tax logic if Tax service is down/slow
- Fully dockerized (Dockerfile + docker-compose)

## How to Run Locally

### Prerequisites
- Docker
- Docker Compose

### Build and Run with Docker Compose

1. Build the Orders service Docker image:
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

### Debugging Redpanda/Kafka

Shows all topics, partitions, and replicas:
```bash
  docker compose exec redpanda rpk topic list
```

Displays broker addresses, cluster ID, controller, and advertised endpoints. Useful for debugging network and advertised address issues.
```bash
  docker compose exec redpanda rpk cluster info
```

Consumes messages from a specified topic. Topic name: `payments.status.updated`
```bash
  docker compose exec redpanda rpk topic consume payments.status.updated -n 5
```

## Environment Variables
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka/Redpanda bootstrap servers (default: `redpanda:9092`)
- `TAX_SERVICE_URL`: URL for Tax service (default: `http://tax:4567/tax/calc`)

---
For more details, see the main repository README.
