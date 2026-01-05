# POC #4 - Mission-Critical Applications

High-availability payment processing system demonstrating **Failover Subscription** for mission-critical applications where duplicate processing must be prevented.

## Architecture

This POC implements a payment processing system using Apache Pulsar's Failover subscription pattern, ensuring exactly-once processing through automatic consumer failover.

### Key Components

- **Payment Model**: Tracks payment lifecycle with retry mechanisms
- **PaymentProducer**: Generates payment transactions using Virtual Threads
- **PaymentProcessor**: Processes payments with Failover subscription and Structured Concurrency
- **Subscription Type**: Failover (only ONE active consumer at a time)

## Failover Subscription Pattern

### How It Works

1. **Active Consumer**: First consumer instance becomes the active processor
2. **Standby Consumers**: Additional instances join as standby (inactive)
3. **Automatic Failover**: If active consumer fails, standby is promoted automatically
4. **Zero Duplicates**: Ensures each payment processed exactly once
5. **Heartbeat Detection**: Failover triggered within ~30 seconds of primary failure

### Why Failover Matters

Mission-critical financial systems require:
- **No duplicate charges**: Each payment processed exactly once
- **High availability**: Service continues even if consumer crashes
- **Guaranteed processing**: Messages never lost, always processed by exactly one consumer
- **Automatic recovery**: No manual intervention needed for failover

## Payment Processing Workflow

### Payment Lifecycle

```
PENDING → PROCESSING → COMPLETED (success)
        ↓
        → REJECTED (validation fails)
        ↓
        → PENDING (retry) → FAILED (max retries exceeded)
```

### Processing Steps (Structured Concurrency)

Three concurrent subtasks coordinated by StructuredTaskScope:

1. **Validation** (50ms)
   - Verify amount > 0 and <= $10,000
   - Validate merchant and customer IDs
   - Check business rules

2. **Transaction Processing** (100-400ms)
   - Small payments (<$100): 100ms
   - Medium payments ($100-$1000): 250ms
   - Large payments (>$1000): 400ms

3. **Audit** (30ms)
   - Record transaction in audit log
   - Best-effort (failures don't block completion)

## Retry Logic

- **Max Retries**: 3 attempts per payment
- **Retry Strategy**: Automatic republish to topic on failure
- **Retry Tracking**: `retryCount` field incremented with each attempt
- **Failure Handling**: After max retries, payment marked FAILED permanently

## Running the POC

### Start Pulsar

```bash
docker-compose up -d
```

### Run Single Instance

```bash
mvn spring-boot:run -pl poc4-mission-critical
```

Application starts on port 8084.

### Test Failover Behavior

**Step 1**: Start primary instance
```bash
mvn spring-boot:run -pl poc4-mission-critical
```

Observe logs:
```
Processor received payment: id=abc-123, merchant=RETAIL-001, amount=150.50 USD
Validating payment: id=abc-123
Processing payment transaction: id=abc-123
Payment completed successfully: id=abc-123
```

**Step 2**: Start secondary instance (different port)
```bash
mvn spring-boot:run -pl poc4-mission-critical -Dserver.port=8085
```

Secondary joins as **standby** - no processing logs appear.

**Step 3**: Kill primary instance (Ctrl+C)

Secondary automatically becomes **active** within 30 seconds:
```
Processor received payment: id=xyz-789, merchant=FINANCE-002, amount=2500.00 USD
```

**Result**: Seamless failover with zero duplicate processing.

## Comparison: Subscription Types

| Type | Active Consumers | Use Case | Duplicate Processing |
|------|-----------------|----------|---------------------|
| **Exclusive** | 1 (no failover) | Single consumer | No |
| **Shared** | Multiple (load balanced) | Worker pools, tasks | Possible on redelivery |
| **Failover** | 1 (with automatic HA) | Mission-critical | **Never** |
| **Key_Shared** | Multiple (per key) | Ordered processing | No (per key) |

**Failover** combines the single-consumer guarantee of Exclusive with automatic high availability.

## Java 25 Features

### Virtual Threads
```java
var executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> producePayment(paymentNum));
```
Lightweight concurrency for payment generation.

### Structured Concurrency
```java
try (var scope = StructuredTaskScope.open()) {
    var validationTask = scope.fork(() -> validatePayment(payment));
    var processingTask = scope.fork(() -> processTransaction(payment));
    var auditTask = scope.fork(() -> auditPayment(payment));
    scope.join();
}
```
Coordinates parallel validation, processing, and audit tasks with unified error handling.

### Switch Expressions
```java
long processingTime = getProcessingTime(payment.getAmount());
```
Determines processing time based on payment amount ranges.

## Configuration

### Pulsar Settings

- **Service URL**: pulsar://localhost:6650
- **Topic**: `persistent://public/default/payment-transactions`
- **Subscription**: `payment-processor-subscription` (Failover)
- **Port**: 8084

### Key Configuration
```yaml
spring:
  pulsar:
    consumer:
      subscription:
        type: failover  # CRITICAL: Enables failover behavior
```

## Expected Output

### Successful Payment
```
14:23:45.123 [pulsar-client-io-1] INFO  PaymentProducer : Produced payment: merchant=RETAIL-001, customer=CUST-0001, amount=150.50 USD
14:23:45.234 [pulsar-listener-1] INFO  PaymentProcessor : Processor received payment: id=abc-123, merchant=RETAIL-001, amount=150.50 USD, retry=0
14:23:45.284 [pulsar-listener-1] DEBUG PaymentProcessor : Validating payment: id=abc-123
14:23:45.335 [pulsar-listener-1] DEBUG PaymentProcessor : Payment validation passed: id=abc-123
14:23:45.336 [pulsar-listener-1] DEBUG PaymentProcessor : Processing payment transaction: id=abc-123, amount=150.50
14:23:45.587 [pulsar-listener-1] DEBUG PaymentProcessor : Payment transaction completed: id=abc-123
14:23:45.617 [pulsar-listener-1] DEBUG PaymentProcessor : Payment audit recorded: id=abc-123
14:23:45.618 [pulsar-listener-1] INFO  PaymentProcessor : Payment completed successfully: id=abc-123, merchant=RETAIL-001, amount=150.50 USD
```

### Failed Payment with Retry
```
14:23:46.123 [pulsar-listener-1] INFO  PaymentProcessor : Processor received payment: id=xyz-789, retry=0
14:23:46.234 [pulsar-listener-1] ERROR PaymentProcessor : Payment processing failed: id=xyz-789, error=Transaction timeout
14:23:46.235 [pulsar-listener-1] WARN  PaymentProcessor : Payment processing failed, will retry: id=xyz-789, retryCount=1/3
14:23:47.456 [pulsar-listener-1] INFO  PaymentProcessor : Processor received payment: id=xyz-789, retry=1
```

## Production Considerations

### High Availability
- Deploy multiple instances across availability zones
- Configure health checks for automatic restart
- Monitor active/standby consumer status

### Monitoring
- Track payment success/failure rates
- Monitor retry patterns and failure reasons
- Alert on failover events
- Measure processing latency by amount range

### Scaling
- Failover subscription scales through redundancy, not parallelism
- For higher throughput, use Key_Shared subscription (partitioned by merchant)
- Consider separate topics for different payment tiers (small/large)

### Retry Strategy
- Current: Exponential backoff could be added
- Consider dead-letter queue for permanently failed payments
- Implement manual review workflow for max-retries-exceeded cases
