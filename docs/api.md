# REST API Documentation

> **See also: [docs/development.md](docs/development.md)**

The Back-End exposes a REST API to manipulate the **Orders** and **Calculate tax**.

## POST /orders

Create an Order, calculate tax via the Tax service, and publish an event to Kafka.
> The event will be published to the topic `payments.status.updated` only if the order is successfully created and tax is calculated.

**params**: n/a

**payload**: JSON object with the following fields:
- `orderId` (string, required): Unique identifier for the order.
- `items` (array of strings, required): List of item names included in the order
- `amount` (number, required): Total amount for the order.
- `currency` (string, required): Currency code (e.g., "USD", "EUR").
- `userId` (string, required): Identifier for the user placing the order.

**returns**: JSON object with the created Order, including a `taxAmount` field returned from the Tax service.

### Request and Response Example

```sh
  # Request example:
  curl -X POST http://localhost:8080/orders \
      -H 'Content-Type: application/json' \
      -d '{
          "orderId": "123",
          "items": ["item1", "item2"],
          "amount": 100.0,
          "currency": "USD",
          "userId": "user-abc"
      }'

  # Response example:
  {
    "orderId": "123",
    "taxAmount": 10.0,
    "status": "success"
  }
```

## POST /tax/calc
Calculate tax for a given amount.

**params**: n/a

**payload**: JSON object with the following fields:
- `amount` (number, required): The amount to calculate tax on.
- `currency` (string, required): Currency code (e.g., "USD", "
EUR").
- `userId` (string, required): Identifier for the user.

**returns**: JSON object with the calculated tax amount.

```sh
  # Request example:
  curl -X POST http://localhost:4567/tax/calc \
      -H 'Content-Type: application/json' \
      -d '{
          "amount": 100.0,
          "currency": "USD"
      }'

  # Response example:
  {
    "taxAmount": 10.0
  }
```