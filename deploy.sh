#!/bin/bash

# ============================================
# Pharmacy Microservices - One-Click Deployment
# ============================================

set -e

echo "============================================"
echo "  Pharmacy Microservices Deployment"
echo "============================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "ERROR: Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "Using Docker Compose V2 (docker compose)..."
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

# Function to display status
show_status() {
    echo ""
    echo "============================================"
    echo "  Services Status"
    echo "============================================"
    $COMPOSE_CMD ps
    echo ""
    echo "============================================"
    echo "  Access URLs"
    echo "============================================"
    echo "  Gateway:        http://localhost:8080"
    echo "  Eureka:         http://localhost:8761"
    echo "  Config Server:  http://localhost:8888"
    echo "  RabbitMQ:       http://localhost:15672"
    echo "  Zipkin:         http://localhost:9411"
    echo "  Swagger (API):  http://localhost:8080/swagger-ui.html"
    echo "============================================"
}

# Parse command
case "${1:-up}" in
    up)
        echo "Starting all services..."
        $COMPOSE_CMD up -d
        echo ""
        echo "Building services (first run may take a while)..."
        $COMPOSE_CMD up -d --build
        echo ""
        echo "Waiting for services to be healthy..."
        sleep 10
        show_status
        ;;
    down)
        echo "Stopping all services..."
        $COMPOSE_CMD down
        ;;
    restart)
        echo "Restarting all services..."
        $COMPOSE_CMD restart
        show_status
        ;;
    logs)
        echo "Showing logs for ${2:-all}..."
        $COMPOSE_CMD logs -f "${2:-}"
        ;;
    status)
        show_status
        ;;
    clean)
        echo "Cleaning up everything..."
        $COMPOSE_CMD down -v --remove-orphans
        echo "Done!"
        ;;
    build)
        echo "Building all services..."
        $COMPOSE_CMD build --no-cache
        ;;
    *)
        echo "Usage: $0 {up|down|restart|logs|status|clean|build}"
        echo ""
        echo "  up      - Start all services (default)"
        echo "  down    - Stop all services"
        echo "  restart - Restart all services"
        echo "  logs    - Show logs (optional: service name)"
        echo "  status  - Show service status"
        echo "  clean   - Stop services and remove volumes"
        echo "  build   - Rebuild all services"
        exit 1
        ;;
esac
