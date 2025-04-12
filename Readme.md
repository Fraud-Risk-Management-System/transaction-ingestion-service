# Transaction Ingestion Service

A high-performance, scalable service for ingesting financial transactions into a Kafka-based fraud detection system.

## Overview

The Transaction Ingestion Service receives transaction data via REST API, converts it to Avro format, and publishes it to Kafka topics for downstream fraud analysis. It includes comprehensive metrics, monitoring, and health checks.

## Features

- REST API for single and batch transaction processing
- Avro serialization with Schema Registry integration
- Kafka production with exactly-once semantics
- Prometheus metrics integration
- Containerized deployment with Docker Compose

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven

## Configuration

The application can be configured via `application.properties` or environment variables:

### Kafka Configuration

```properties
spring.kafka.bootstrap-servers=localhost:29092
spring.kafka.producer.properties.schema.registry.url=http://localhost:8081
kafka.topics.transactions=banking-transactions
```

> **Important**: When running the application locally (outside Docker), use `localhost:29092` for the Kafka bootstrap servers. When running inside Docker, use `kafka:9092`.

## Running Locally

1. Start the infrastructure services:
   ```bash
   docker-compose up -d zookeeper kafka schema-registry prometheus grafana
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Building and Running with Docker

### Building for Multiple Architectures

This project supports both ARM64 (e.g., Apple Silicon) and x86-64 architectures. To build for both:

1. Set up Docker buildx (one-time setup):
   ```bash
   docker buildx create --name mybuilder --use
   ```

2. Build and push multi-architecture image:
   ```bash
   docker buildx build --platform linux/amd64,linux/arm64 -t yourusername/transaction-ingestion-service:latest --push .
   ```

   Replace `yourusername` with your container registry username.

### Running with Docker Compose

```bash
docker-compose up -d
```

## API Usage

### Process a Single Transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TX123456789",
    "accountId": "ACC987654321",
    "customerId": "CUST123456",
    "sourceId": "ATM_NYC_001",
    "transactionType": "WITHDRAWAL",
    "amount": 499.99,
    "currency": "USD",
    "timestamp": 1742598645123,
    "merchantName": "Online Retailer Inc.",
    "merchantCategory": "RETAIL",
    "cardPresent": false,
    "ipAddress": "192.168.1.100",
    "deviceId": "DEVICE-ABC123",
    "location": {
      "latitude": 37.7749,
      "longitude": -122.4194,
      "country": "US",
      "city": "San Francisco"
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
      "accountId": "ACC987654321",
      "customerId": "CUST123456",
      "sourceId": "ATM_NYC_001",
      "transactionType": "WITHDRAWAL",
      "amount": 499.99,
      "currency": "USD",
      "timestamp": 1742598645123,
      "merchantName": "Online Retailer Inc.",
      "merchantCategory": "RETAIL",
      "cardPresent": false,
      "ipAddress": "192.168.1.100",
      "deviceId": "DEVICE-ABC123"
    }
  ]'
```

## Monitoring

- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## Troubleshooting

### Kafka Connection Issues

If you encounter Kafka connection issues, make sure:

1. Your bootstrap server settings match your environment:
   - Local development: `spring.kafka.bootstrap-servers=localhost:29092`
   - Docker: `spring.kafka.bootstrap-servers=kafka:9092`

2. The Kafka and Zookeeper containers are healthy:
   ```bash
   docker-compose ps
   ```

3. You can connect to Kafka from within the container:
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --list
   ```

### Inspecting Kafka Messages

To view messages in the Kafka topic:

```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic banking-transactions \
  --from-beginning
```

For Avro-formatted messages:

```bash
docker exec -it schema-registry kafka-avro-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic banking-transactions \
  --from-beginning \
  --property schema.registry.url=http://schema-registry:8081
```

### Missing Dependencies

If you encounter Bean creation exceptions related to missing AspectJ:

```
Error creating bean with name 'timedAspect': Factory method 'timedAspect' threw exception with message: org/aspectj/lang/NoAspectBoundException
```

Make sure you have the Spring AOP dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.