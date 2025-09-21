
.PHONY: build up down clean restart log ps \
	orders-test orders-test-contract orders-test-unit orders-test-integration \
	tax-test-unit

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

orders-test: orders-test-contract orders-test-unit
	@echo "\n========= All tests completed ========= "

orders-test-contract:
	@echo "========= [orders-java] Running contract tests =========\n"
	docker compose run --rm orders-contract-tests
	@echo "\n========= [orders-java] Contract tests completed ========= "

orders-test-unit:
	@echo "========= [orders-java] Running unit tests =========\n"
	docker compose run --rm orders-unit-tests
	@echo "\n========= [orders-java] Unit tests completed ========= "

orders-test-integration:
	@echo "========= [orders-java] Running integration tests =========\n"
	docker compose run --rm orders-integration-tests
	@echo "\n========= [orders-java] Integration tests completed ========= "

tax-test-unit:
	@echo "========= [tax-ruby] Running unit tests =========\n"
	docker compose run --rm tax-unit-tests
	@echo "\n========= [tax-ruby] Unit tests completed ========= "
