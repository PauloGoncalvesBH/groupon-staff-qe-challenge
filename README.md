
# Groupon Staff QE Challenge

End-to-end sample showcasing a consumer-provider setup with Spring Boot (Orders), Sinatra (Tax), Kafka/Redpanda, unit tests, Pact contracts,  and integration tests with Testcontainers.

- **orders-java** - Orders service (Java/Spring Boot)
- **tax-ruby** - Tax service (Ruby/Sinatra)

## Documentation

- [docs/development.md](docs/development.md) - Development, architecture flow, technologies, testing, and CI.
- [docs/api.md](docs/api.md) - REST API documentation.

## Application behavior

The application consists of two services: Orders and Tax.

The Orders service exposes a REST API endpoint `POST /orders` that accepts order details including `orderId`, `items`, `amount`, `currency`, and `userId`. Upon receiving an order, it calls the Tax service's `POST /tax/calc` endpoint to calculate the tax based on the provided amount and currency.

The Tax service responds with the calculated tax amount, which the Orders service includes in the payment status update event published to the `payments.status.updated` topic.

The topic will be published to Kafka/Redpanda only if  `POST /orders` is successful and tax is calculated.

### Tax calculation rules

1. Tax for currency "*USD*" is 10% of the amount.
1. Tax for other currencies is 8% of the amount.
1. Flat rate fallback tax is 10% of the amount.

**Fallback:** If the Tax service is slow or down, the Orders service applies a fallback tax (*10%*) and still completes the order process, ensuring that the order is processed even in the absence of a successful tax calculation.

> Tax calculation are basic and these rules were implemented only to enable more testing scenarios.

## Next steps

### Security

1. Implement input validation and sanitization to prevent common security vulnerabilities (e.g., SQL injection, XSS).
1. Remove fingerprinting information (e.g., server version) from HTTP headers.
1. Apply patch updates for base images.

> More details: [API Security Best Practices](https://github.com/shieldfy/API-Security-Checklist) and [OWASP - REST Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)

### Quality / Maintainability

1. Add code analysis tools (e.g., SonarQube) to enforce coding standards and detect code smells.
1. Add linters/formatters.
1. Adopt pact-broker to manage contracts between services.

### Continuous Integration (CI)

1. Add docker caching to the CI workflows to speed up build times.

### Monitoring

1. Implement observability tools (e.g. Datadog) to monitor application performance and health, *with alerting for critical issues*.

### Business requirements

1. Map the requirements to calculate tax (e.g., different tax rates based on item type, etc.).
1. Map how the topic `payments.status.updated` will be used by other services to define the event schema and required fields.
