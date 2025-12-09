#!/bin/bash

# Test runner script for microservices
# Usage: ./scripts/test-service.sh [auth|inventory|orders]

set -e

SERVICE=${1:-}

if [ -z "$SERVICE" ]; then
    echo "Usage: $0 [auth|inventory|orders]"
    exit 1
fi

if [ "$SERVICE" != "auth" ] && [ "$SERVICE" != "inventory" ] && [ "$SERVICE" != "orders" ]; then
    echo "Error: Service must be one of: auth, inventory, orders"
    exit 1
fi

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Running tests for service: $SERVICE"
echo "Project root: $PROJECT_ROOT"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Run tests using docker-compose
cd "$PROJECT_ROOT"
docker compose -f docker-compose.test.yml run --rm "test-$SERVICE"

