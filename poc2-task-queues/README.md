# POC #2: Distributed Task Queue

## What It Does

Demonstrates distributed task queue implementation with Apache Pulsar. Tasks are produced with different priorities and types, then processed by multiple workers using structured concurrency.

## Key Technologies

- **Java 25 Structured Concurrency**: Manages concurrent subtasks with automatic cleanup
- **Virtual Threads**: High-throughput task processing
- **Spring Pulsar**: Integration with Pulsar messaging
- **Shared Subscriptions**: Load distribution across multiple workers

## Architecture

Single task queue topic:
```
persistent://public/default/task-queue
```

## Components

**TaskProducer**: Generates 10 tasks with varying types (EMAIL, REPORT, BACKUP) and priorities

**TaskWorker**: Processes tasks using structured concurrency with three concurrent subtasks:
- Validation: Verifies task data integrity
- Execution: Performs the actual task work
- Audit: Records task processing for tracking

**Task**: Event model with taskType, payload, priority, status, and retryCount

## Running

```bash
docker-compose up -d
cd poc2-task-queues
mvn spring-boot:run
```

Application runs on port 8082 and processes tasks for ~10 seconds with 3 concurrent workers.

## Output

Workers process tasks concurrently:
```
Worker received task: type=EMAIL, priority=10, id=...
Task completed successfully: id=..., type=EMAIL
Worker received task: type=REPORT, priority=5, id=...
Task completed successfully: id=..., type=REPORT
```

## Structured Concurrency Benefits

- Automatic error propagation from subtasks
- Guaranteed cleanup even on failure
- Clear task lifecycle boundaries
- No thread leaks
