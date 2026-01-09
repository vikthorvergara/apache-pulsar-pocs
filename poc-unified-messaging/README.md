# POC: Unified Messaging Platform

Single payment processing topic demonstrating all three messaging patterns: Pub/Sub, Queueing, and Streaming.

## Architecture

**Single Topic**: `persistent://public/default/payments`

### Messaging Patterns

**Pub/Sub (Broadcast)**
- FraudDetectionService (Exclusive subscription)
- AccountingService (Exclusive subscription)
- NotificationService (Exclusive subscription)
- Each service receives ALL payment messages

**Queueing (Load Balancing)**
- PaymentProcessingWorker (Shared subscription, 3 workers)
- Each payment processed by exactly ONE worker

**Streaming (Replay & Analytics)**
- AnalyticsService (Reader API from earliest)
- AuditService (Reader API for compliance)

## Prerequisites

- Java 25 with preview features enabled
- Apache Pulsar running locally
- Maven 3.9+

## Setup

### Start Pulsar

```bash
docker run -d \
  --name pulsar-standalone \
  -p 6650:6650 \
  -p 8080:8080 \
  apachepulsar/pulsar:latest \
  bin/pulsar standalone
```

Wait for Pulsar to be ready:

```bash
until curl -s http://localhost:8080/admin/v2/namespaces/public/default > /dev/null 2>&1; do
  echo "Waiting for Pulsar..."
  sleep 1
done
echo "Pulsar is ready"
```

## Build

From the project root:

```bash
./mvnw clean install
```

Or build only POC:

```bash`
./mvnw clean install -pl poc-unified-messaging -am
```

## Run

From the poc-unified-messaging directory:

```bash
cd poc-unified-messaging
../mvnw spring-boot:run
```

Or from the project root:

```bash
./mvnw spring-boot:run -pl poc-unified-messaging
```

## Expected Output

You should see all three patterns working simultaneously:

### Pub/Sub Pattern
```
[PUB/SUB-FRAUD] Received payment: id=abc123, merchant=RETAIL-001, amount=150.50 USD
[PUB/SUB-FRAUD] Fraud check completed: id=abc123, score=90, decision=APPROVED

[PUB/SUB-ACCOUNTING] Received payment: id=abc123, merchant=RETAIL-001, amount=150.50 USD
[PUB/SUB-ACCOUNTING] Accounting completed: id=abc123, tax=15.05, net=135.45

[PUB/SUB-NOTIFY] Received payment: id=abc123, merchant=RETAIL-001, customer=CUST-0001
[PUB/SUB-NOTIFY] All notifications sent: id=abc123
```

### Queue Pattern
```
[QUEUE-WORKER] Worker pool-1-thread-1 received payment: id=abc123, amount=150.50 USD
[QUEUE-WORKER] Worker pool-1-thread-1 completed payment: id=abc123

[QUEUE-WORKER] Worker pool-1-thread-2 received payment: id=def456, amount=75.25 USD
[QUEUE-WORKER] Worker pool-1-thread-2 completed payment: id=def456
```

### Streaming Pattern
```
[STREAMING-ANALYTICS] Summary: payments=5, totalVolume=2910.75, avgAmount=582.15, topMerchant=RETAIL-001

[STREAMING-AUDIT] Audit Report Generated:
[STREAMING-AUDIT]   Total Payments Audited: 20
[STREAMING-AUDIT]   Merchant Transaction Counts:
[STREAMING-AUDIT]     RETAIL-001 -> 5 transactions
[STREAMING-AUDIT]     FINANCE-002 -> 5 transactions
[STREAMING-AUDIT]   High-Value Payments (>$5000): 3
```

## Key Observations

**Same Payment, Different Patterns**
- All 3 pub/sub services process payment #1
- Only 1 queue worker processes payment #1
- Analytics and audit services read all payments

**Load Balancing**
- Watch the worker thread names in queue pattern
- Different threads handle different payments

**Real-time Analytics**
- Analytics service shows running totals every 5 payments
- Demonstrates streaming consumption

**Replay Capability**
- Audit service reads from beginning of topic
- Can replay payment history for compliance

## Cleanup

Stop the application:

```bash
Ctrl+C
```

Stop and remove Pulsar:

```bash
docker stop pulsar-standalone
docker rm pulsar-standalone
```

## What This Demonstrates

**Unified Platform**: One topic, three consumption patterns
- Replaces Kafka (streaming) + RabbitMQ (queueing) + traditional pub/sub

**Flexibility**: Same messages consumed differently based on business needs
- Fraud/Accounting/Notifications need all messages (pub/sub)
- Payment processing needs load balancing (queue)
- Analytics/Audit need replay capability (streaming)

**Java 25 Features**:
- Structured Concurrency for parallel operations in all consumers
- Virtual Threads for lightweight concurrency in producer
- Modern switch expressions and pattern matching

## Troubleshooting

**Port already in use**
```bash
docker ps -a
docker stop pulsar-standalone
docker rm pulsar-standalone
```

**Connection refused**
```bash
curl http://localhost:8080/admin/v2/namespaces/public/default
```

If Pulsar is not responding, restart the container.

**Build failures**
Ensure Java 25 with preview features is enabled:
```bash
java --version
echo $JAVA_HOME
```
