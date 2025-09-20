
.PHONY: build up down clean

build:
	docker compose build orders

up:
	docker compose up --build orders

down:
	docker compose down

restart: down up

clean:
	docker system prune -f

log:
	docker compose logs --follow orders

ps:
	docker compose ps

# Tests

orders-test: orders-test-contract

orders-test-contract:
	@echo "========= Running contract tests (orders) =========\n"
	docker compose run --rm orders-contract-tests
	@echo "\n========= Contract tests (orders) completed ========= "
