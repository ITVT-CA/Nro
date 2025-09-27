#!/bin/bash

echo "Starting NRO Base Server with Docker Compose..."
echo "Using existing MariaDB database on mariadb:3306"

mkdir -p logs

# Start the service
docker compose up -d

echo "NRO Server started!"
echo "Using existing MariaDB database on mariadb:3306"
echo "NRO Server is running on port 14445"
echo ""
echo "To view logs: docker compose logs -f nro_server"
echo "To stop service: docker compose down"
echo "To restart: docker compose restart"