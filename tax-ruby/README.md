# Tax Service (Ruby - Sinatra)

Minimal Tax calculation service for the challenge. Exposes POST /tax/calc that returns a computed taxAmount.

## Tax Calculation Logic

- For currency "USD", returns 10% of the amount.
- For other currencies, returns 8% of the amount.
- If the service is down/unavailable, the fallback returns a fixed tax of 10.0.

## Endpoints

- POST /tax/calc
  - Request JSON: { amount, currency }
  - Response JSON: { taxAmount }

- GET /health → { status: 'ok', time }

## Testes unitários

Execute com Makefile (recomendado):

```bash
make tax-test-unit
```

Ou diretamente via Docker Compose:

```bash
docker compose run --rm --profile test tax-unit-tests
```
