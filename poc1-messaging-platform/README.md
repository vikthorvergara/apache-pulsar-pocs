# POC1 - Messaging Platform

Multi-tenant messaging platform demonstrating Apache Pulsar integration with Spring Boot, showcasing tenant isolation through dedicated topics and shared subscriptions for scalability.

## Overview

This POC demonstrates a multi-tenant messaging architecture where three tenants (healthcare, finance, and retail) operate in complete isolation, each with their own Pulsar namespace and topics.

## Technologies

- **Java 25**: Scoped Values for tenant context propagation, Virtual Threads for concurrency
- **Spring Boot 4.0**: Application framework
- **Spring Pulsar 2.0**: Pulsar integration
- **Apache Pulsar 4.0**: Message broker with multi-tenancy support
- **Docker**: Containerized Pulsar deployment

## Architecture

### Topic Structure
Each tenant has an isolated namespace with dedicated topics:

```
persistent://healthcare/events/patient-events
persistent://finance/events/transaction-events
persistent://retail/events/order-events
```

### Components

- **MultiTenantProducer**: Publishes 5 events per tenant using Virtual Threads and Scoped Values for tenant context
- **TenantEventListener**: Three separate listeners (one per tenant) with shared subscriptions for load balancing
- **TenantEvent**: Event model with tenant ID, event type, and payload

## Prerequisites

- Java 25
- Docker & Docker Compose
- Maven 3.9+

## Setup & Running

### 1. Start Apache Pulsar

From the project root directory:

```bash
cd ..
docker-compose up -d
```

This starts Pulsar standalone mode with:
- Broker port: `6650`
- Admin API port: `8080`
- Web UI: http://localhost:8080

### 2. Setup Pulsar Tenants and Namespaces

Run the setup script to create the required tenants and namespaces:

```bash
cd poc1-messaging-platform
./setup-pulsar.sh
```

This script will:
- Wait for Pulsar to be healthy
- Create three tenants: `healthcare`, `finance`, `retail`
- Create a namespace for each tenant: `<tenant>/events`
- Verify the setup

**Manual Setup (Alternative)**

If you prefer to set up manually:

```bash
# Wait for Pulsar to be ready
docker exec pulsar bin/pulsar-admin brokers healthcheck

# Create tenants
docker exec pulsar bin/pulsar-admin tenants create healthcare
docker exec pulsar bin/pulsar-admin tenants create finance
docker exec pulsar bin/pulsar-admin tenants create retail

# Create namespaces
docker exec pulsar bin/pulsar-admin namespaces create healthcare/events
docker exec pulsar bin/pulsar-admin namespaces create finance/events
docker exec pulsar bin/pulsar-admin namespaces create retail/events

# Verify setup
docker exec pulsar bin/pulsar-admin tenants list
docker exec pulsar bin/pulsar-admin namespaces list healthcare
docker exec pulsar bin/pulsar-admin namespaces list finance
docker exec pulsar bin/pulsar-admin namespaces list retail
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The application runs on port `8081` and executes for approximately 5 seconds, producing interleaved log output from all three tenants demonstrating concurrent message processing.

## Expected Output

```
Publishing Healthcare Event 1...
Publishing Finance Event 1...
Publishing Retail Event 1...
Received Healthcare event: {...}
Received Finance event: {...}
Received Retail event: {...}
...
```

## Troubleshooting

### Namespace Not Found Error

If you see `Namespace not found` errors:
1. Ensure Pulsar is running: `docker ps | grep pulsar`
2. Check Pulsar health: `docker exec pulsar bin/pulsar-admin brokers healthcheck`
3. Run the setup script: `./setup-pulsar.sh`

### Connection Refused

If you see connection refused errors:
1. Check if Pulsar is running: `docker ps`
2. Verify port 6650 is accessible: `netstat -an | grep 6650`
3. Check Pulsar logs: `docker logs pulsar`

### Topics Not Created

Topics are created automatically by Pulsar when the first message is published (auto-creation is enabled by default in standalone mode).

## Cleanup

To stop and remove Pulsar:

```bash
cd ..
docker-compose down -v
```

The `-v` flag removes the volumes, ensuring a clean state for the next run.

## Configuration

Key configuration in `application.yml`:

```yaml
spring:
  pulsar:
    client:
      service-url: pulsar://localhost:6650
    producer:
      producer-name: ${spring.application.name}-producer
      send-timeout: 30s
    consumer:
      subscription:
        type: shared
    listener:
      schema-type: json
```
