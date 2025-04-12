#!/bin/bash
# build-and-deploy.sh - Script to build and deploy the Transaction Ingestion Service

set -e  # Exit on any error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Building and deploying Transaction Ingestion Service${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo -e "${RED}Error: Docker is not running or not accessible${NC}"
  exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose > /dev/null 2>&1; then
  echo -e "${RED}Error: Docker Compose is not installed${NC}"
  exit 1
fi

# Create necessary directories
echo -e "${YELLOW}Creating configuration directories...${NC}"
mkdir -p prometheus grafana/provisioning/datasources grafana/provisioning/dashboards/json

# Copy configuration files
echo -e "${YELLOW}Copying configuration files...${NC}"
cp prometheus-config.yml prometheus/prometheus.yml
cp grafana-datasource.yml grafana/provisioning/datasources/datasource.yml
cp grafana-dashboard.yml grafana/provisioning/dashboards/dashboard.yml
cp transaction-service-dashboard.json grafana/provisioning/dashboards/json/transaction-service-dashboard.json

# Build the application
echo -e "${YELLOW}Building the application...${NC}"
mvn clean package -DskipTests

# Build the Docker image
echo -e "${YELLOW}Building Docker image...${NC}"
docker build -t transaction-ingestion-service:latest .

# Deploy using Docker Compose
echo -e "${YELLOW}Deploying with Docker Compose...${NC}"
docker-compose up -d

# Check if services are running
echo -e "${YELLOW}Checking if services are running...${NC}"
sleep 10  # Give services time to start

if docker ps | grep -q "transaction-ingestion-service"; then
  echo -e "${GREEN}Transaction Ingestion Service is running!${NC}"
else
  echo -e "${RED}Transaction Ingestion Service failed to start. Check logs with: docker-compose logs transaction-ingestion-service${NC}"
fi

echo -e "${GREEN}Deployment complete!${NC}"
echo -e "Access the service at: http://localhost:8080"
echo -e "Access Grafana at: http://localhost:3000 (admin/admin)"
echo -e "Access Prometheus at: http://localhost:9090"