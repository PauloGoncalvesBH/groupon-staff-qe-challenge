
build:
	docker compose build

start:
	docker compose up --build orders

down:
	docker compose down

restart: down start

clean:
	docker system prune -f

ps:
	docker compose ps

# Tests

orders-test: orders-test-contract orders-test-integration orders-test-unit
	@echo "\n========= [orders-java] All tests completed ========= "

orders-test-unit:
	@echo "========= [orders-java] Running unit tests =========\n"
	docker compose run --rm orders-unit-tests
	@echo "\n========= [orders-java] Unit tests completed ========= "

orders-test-integration:
	@echo "========= [orders-java] Running integration tests =========\n"
	docker compose run --rm orders-integration-tests
	@echo "\n========= [orders-java] Integration tests completed ========= "

orders-test-contract:
	@echo "========= [orders-java] Running contract tests =========\n"
	docker compose run --rm orders-contract-tests
	@echo "\n========= [orders-java] Contract tests completed ========= "

tax-test: tax-test-unit tax-test-contract
	@echo "\n========= [tax-ruby] All tests completed ========= "

tax-test-unit:
	@echo "========= [tax-ruby] Running unit tests =========\n"
	docker compose run --rm tax-unit-tests
	@echo "\n========= [tax-ruby] Unit tests completed ========= "

tax-test-contract:
	@echo "========= [tax-ruby] Running provider contract tests =========\n"
	docker compose run --rm tax-contract-tests
	@echo "\n========= [tax-ruby] Provider contract tests completed ========= "
