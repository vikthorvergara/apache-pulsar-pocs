# Messaging Systems - Easy Comparison

## Apache Kafka

**Best For:** Event streaming, high-throughput data pipelines, event sourcing
- **Architecture:** Distributed commit log (append-only, partitioned topics)
- **Throughput:** Up to 2M+ messages/sec (highest of all)
- **Latency:** Moderate (200K msg/s at low latency, degrades at lower throughput)
- **Persistence:** Yes - infinite retention on disk, replayable
- **Ordering:** Strict per-partition ordering guaranteed
- **Delivery:** At-least-once by default, exactly-once with transactions
- **Scalability:** Horizontal (add brokers), but partition rebalancing can be painful
- **Complexity:** Medium (single system after ZooKeeper removal via KIP-500)
- **Use Cases:** Real-time analytics, log aggregation, CDC, event-driven microservices

## RabbitMQ

**Best For:** Traditional messaging, task queues, request-reply patterns
- **Architecture:** Message broker with exchanges/queues (AMQP protocol)
- **Throughput:** Up to 60K messages/sec (lowest of group)
- **Latency:** Lowest latency at lower throughput (<30K msg/s)
- **Persistence:** Yes - messages stored until consumed, then deleted
- **Ordering:** Yes per queue
- **Delivery:** At-least-once, manual acks, publisher confirms
- **Scalability:** Vertical + clustering (limited horizontal scaling)
- **Complexity:** Low (easy setup, familiar patterns)
- **Use Cases:** Microservices RPC, work distribution, priority queues, AMQP integrations

## NATS (with JetStream)

**Best For:** Lightweight pub/sub, IoT, edge computing, request-reply
- **Architecture:** Message bus (core) + optional streaming (JetStream)
- **Throughput:** Up to 6M+ messages/sec (fastest for fire-and-forget)
- **Latency:** Extremely low (<1ms for core NATS)
- **Persistence:** Optional via JetStream (core NATS is fire-and-forget)
- **Ordering:** Per subject/stream (not global)
- **Delivery:** At-most-once (core), at-least-once (JetStream)
- **Scalability:** Clustering + leaf nodes + superclusters (complex at scale)
- **Complexity:** Low for core, medium for JetStream multi-region
- **Use Cases:** Microservices mesh, IoT telemetry, command/control, edge deployments

## Apache Pulsar

**Best For:** Multi-tenant platforms, geo-replication, unified streaming + queuing
- **Architecture:** Layered (brokers + BookKeeper storage + ZooKeeper)
- **Throughput:** Up to 800K-2M messages/sec (middle ground)
- **Latency:** Higher than Kafka at same throughput (due to layered architecture)
- **Persistence:** Yes - tiered storage (hot on BookKeeper, cold on S3/GCS)
- **Ordering:** Flexible (Exclusive/Failover = strict, Shared/Key_Shared = parallel)
- **Delivery:** At-least-once, no native exactly-once transactions
- **Scalability:** Best separation of compute/storage (scale independently)
- **Complexity:** High (4 systems: Pulsar, BookKeeper, ZooKeeper, RocksDB)
- **Use Cases:** Multi-tenant SaaS, active-active geo-replication, infinite retention

---

## Quick Decision Matrix

| Need | Choose |
|------|--------|
| Highest throughput + event sourcing | **Kafka** |
| Lowest latency for simple pub/sub | **NATS Core** |
| Traditional message broker patterns | **RabbitMQ** |
| Multi-tenancy + geo-replication | **Pulsar** |
| Simplest operations | **NATS** or **RabbitMQ** |
| Best Spring Boot ecosystem | **Kafka** > **RabbitMQ** > **Pulsar** > **NATS** |
| Replay historical data | **Kafka** or **Pulsar** (not RabbitMQ/NATS core) |
| Edge/IoT deployments | **NATS** |
| Exactly-once guarantees | **Kafka** (only one with native support) |
