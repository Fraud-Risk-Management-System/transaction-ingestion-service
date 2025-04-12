# Transaction Ingestion Service

A high-performance, scalable service for ingesting financial transactions into a Kafka-based fraud detection system.

## Overview

The Transaction Ingestion Service receives transaction data via REST API, converts it to Avro format, and publishes it to Kafka topics for downstream fraud analysis. It includes comprehensive metrics, monitoring, and health checks.

## Features

- **REST API**: Endpoints for single and batch transaction processing
- **Avro Serialization**: Schema-based serialization with Schema Registry integration
- **Kafka Integration**: High-throughput message production with exactly-once semantics
- **Metrics & Monitoring**: Prometheus integration with custom metrics
- **Containerization**: Docker and Docker Compose for easy deployment
- **Validation**: Comprehensive input validation for all transaction data
- **Performance**: Optimized for high-throughput with proper batching and async processing

## Architecture

This service is part of a larger Fraud Risk Management system:

```
                 ┌─────────────────┐
                 │ API Gateway     │
                 └────────┬────────┘
                          │
                          ▼
┌─────────────────────────────────────────┐
│ Transaction Ingestion Service           │
│                                         │
│  ┌─────────┐    ┌─────────┐    ┌──────┐ │
│  │ REST API├───►│ Services├───►│Kafka │ │
│  └─────────┘    └─────────┘    └──────┘ │
└─────────────────────┬───────────────────┘
                      │
                      ▼
               ┌─────────────┐
               │ Kafka       │
               └──────┬──────┘
                      │
                      ▼
          ┌───────────────────────┐
          │ Rule Engine Service   │
          └───────────────────────┘
```

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven 3.6+

## Quick Start

### Using Docker Compose (Recommended)

The easiest way to run the entire stack:

```bash
# Clone the repository
git clone https://github.com/Fraud-Risk-Management-System/transaction-ingestion-service.git
cd transaction-ingestion-service

# Start all services
docker-compose up -d
```

This starts:
- Zookeeper
- Kafka
- Schema Registry
- Transaction Ingestion Service
- Prometheus
- Grafana

### Local Development

For development, you might want to run the services individually:

1. **Start the infrastructure:**
   ```bash
   docker-compose up -d zookeeper kafka schema-registry
   ```

2. **Generate Avro classes:**
   ```bash
   ./mvnw generate-sources
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

### IDE Setup

When working with this project in an IDE:

1. Run `./mvnw generate-sources` first to generate Avro classes
2. Make sure `target/generated-sources/avro` is marked as a source root
3. For IntelliJ IDEA:
   - Go to File > Project Structure > Modules
   - Find the `target/generated-sources/avro` directory
   - Mark it as "Sources"

## Configuration

The application can be configured via `application.properties` or environment variables:

### Kafka Configuration

```properties
# When running locally (outside Docker)
spring.kafka.bootstrap-servers=localhost:29092

# When running inside Docker
spring.kafka.bootstrap-servers=kafka:9092

# Schema Registry
spring.kafka.producer.properties.schema.registry.url=http://localhost:8081  # Or http://schema-registry:8081 in Docker
```

### Performance Tuning

```properties
# Kafka producer settings
spring.kafka.producer.batch-size=32768
spring.kafka.producer.linger-ms=5
spring.kafka.producer.buffer-memory=67108864
spring.kafka.producer.compression-type=lz4

# Server settings
server.tomcat.max-threads=200
server.tomcat.max-connections=10000
```

## API Usage

### Process a Single Transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TX123456789",
    "timestamp": "2023-03-15T14:30:00.000Z",
    "amount": 499.99,
    "currency": "USD",
    "customerId": "CUST123456",
    "sourceId": "ATM_NYC_001",
    "transactionType": "WITHDRAWAL",
    "destinationId": "MERCHANT-456",
    "destinationType": "MERCHANT",
    "metadata": {
      "ipAddress": "192.168.1.100",
      "deviceId": "DEVICE-ABC123",
      "location": {
        "latitude": 37.7749,
        "longitude": -122.4194
      }
    }
  }'
```

### Process a Batch of Transactions

```bash
curl -X POST http://localhost:8080/api/v1/transactions/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "transactionId": "TX123456789",
      "amount": 499.99,
      "currency": "USD",
      "customerId": "CUST123456",
      "sourceId": "ATM_NYC_001",
      "transactionType": "WITHDRAWAL"
    },
    {
      "transactionId": "TX123456790",
      "amount": 299.99,
      "currency": "USD",
      "customerId": "CUST123456",
      "sourceId": "ATM_NYC_001",
      "transactionType": "WITHDRAWAL"
    }
  ]'
```

## Monitoring

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

### Key Metrics

- `transactions.received`: Total transactions received
- `transactions.processed`: Successfully processed transactions
- `transactions.failed`: Failed transactions
- `transactions.processing.time`: Processing time histogram
- `kafka.producer.success`: Successful Kafka sends
- `kafka.producer.failure`: Failed Kafka sends
- `transactions.amount`: Transaction amount distribution

## Troubleshooting

### Common Issues

#### Kafka Connection Issues

If you encounter Kafka connection issues:

1. Check bootstrap server settings:
   - Local development: `spring.kafka.bootstrap-servers=localhost:29092`
   - Docker: `spring.kafka.bootstrap-servers=kafka:9092`

2. Verify Kafka containers are healthy:
   ```bash
   docker-compose ps
   ```

3. Test Kafka connectivity:
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --list
   ```

#### Schema Registry Issues

1. Check Schema Registry is running:
   ```bash
   docker-compose ps schema-registry
   ```

2. Test Schema Registry connectivity:
   ```bash
   curl http://localhost:8081/subjects
   ```

#### Avro Generation Issues

If you encounter "Cannot find class" errors:

1. Regenerate Avro classes:
   ```bash
   ./mvnw clean generate-sources
   ```

2. Ensure your IDE recognizes the generated sources:
   - Mark `target/generated-sources/avro` as a source root
   - Reload the project in your IDE

### Viewing Kafka Messages

To view messages in the Kafka topic:

```bash
# View raw messages (might be binary due to Avro)
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic banking-transactions \
  --from-beginning

# View decoded Avro messages
docker exec -it schema-registry kafka-avro-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic banking-transactions \
  --from-beginning \
  --property schema.registry.url=http://schema-registry:8081
```

## Development Guide

### Project Structure

```
transaction-ingestion-service/
├── src/
│   ├── main/
│   │   ├── java/com/fraudrisk/
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── exception/      # Exception classes
│   │   │   ├── mapper/         # Data mappers
│   │   │   ├── model/          # Domain models
│   │   │   ├── service/        # Business logic
│   │   │   ├── util/           # Utility classes
│   │   │   └── TransactionIngestionServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties  # Application config
│   │       └── avro/                   # Avro schemas
│   └── test/                           # Test classes
├── docker/                             # Docker configurations
│   ├── grafana/
│   └── prometheus/
├── docker-compose.yml                  # Service definitions
├── Dockerfile                          # Service image build
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

### Working with Avro

Remember these key points when working with Avro:

1. Define schemas in `src/main/resources/avro/*.avsc` files
2. Generate Java classes with `./mvnw generate-sources`
3. Never modify generated classes directly
4. Follow schema evolution best practices for compatibility

### Adding New Features

1. Define new DTOs for your API endpoints
2. Update or create controllers as needed
3. Implement business logic in services
4. Add appropriate tests
5. Consider adding metrics for new functionality

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.