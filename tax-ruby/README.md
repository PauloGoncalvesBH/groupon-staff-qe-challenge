# Tax Service (Ruby - Sinatra)

Minimal Tax calculation service for the challenge. Exposes POST /tax/calc that returns a computed taxAmount.

## Tax Calculation Logic

- For currency "USD", returns 10% of the amount.
- For other currencies, returns 8% of the amount.
- If the service is down/unavailable, the fallback returns a fixed tax of 10.0.

## Endpoints

- POST /tax/calc
  - Request JSON: { amount, currency, userId }
  - Response JSON: { taxAmount }

## Run with Docker

Build and run using docker compose (from repo root):
```bash
# build and start all services
docker compose up --build -d

# or build just tax-ruby image
docker build -t tax-ruby:local ./tax-ruby
```

Service listens on port 4567 inside the compose network, reachable at http://tax:4567.

## Test locally (optional)

```bash
curl -X POST http://localhost:4567/tax/calc \
  -H 'Content-Type: application/json' \
  -d '{"amount": 100.0, "currency": "USD", "userId": "u1"}'
```
