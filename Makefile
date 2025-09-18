
.PHONY: build up down clean

build:
	docker-compose build orders

up:
	docker-compose up --build

down:
	docker-compose down

restart: down up

clean:
	docker system prune -f

log:
	docker-compose logs --follow orders

ps:
	docker-compose ps