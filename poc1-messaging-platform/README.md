# POC #1: Multi-Tenant Messaging Platform

## What It Does

Demonstrates multi-tenant messaging with Apache Pulsar. Three independent tenants (healthcare, finance, retail) publish and consume events through isolated topics.

## Key Technologies

- **Java 25 Scoped Values**: Manages tenant context without ThreadLocal overhead
- **Virtual Threads**: Concurrent event publishing for all tenants
- **Spring Pulsar**: Integration with Pulsar messaging
- **Shared Subscriptions**: Load distribution across consumers

## Architecture

Each tenant has a dedicated topic:
```
persistent://healthcare/events/patient-events
persistent://finance/events/transaction-events
persistent://retail/events/order-events
```

## Components

**MultiTenantProducer**: Publishes 5 events per tenant using virtual threads and scoped values

**TenantEventListener**: Three listeners, one per tenant, using shared subscriptions

**TenantEvent**: Event model with tenantId, eventName, and data

## Running

```bash
docker-compose up -d
cd poc1-messaging-platform
mvn spring-boot:run
```

Application runs on port 8081 and publishes/consumes events for ~5 seconds.

## Output

Interleaved logs from all tenants:
```
[HEALTHCARE] Received: TenantEvent{tenantId='healthcare', eventName='PatientRegistered', ...}
[FINANCE] Received: TenantEvent{tenantId='finance', eventName='TransactionCreated', ...}
[RETAIL] Received: TenantEvent{tenantId='retail', eventName='OrderPlaced', ...}
```
