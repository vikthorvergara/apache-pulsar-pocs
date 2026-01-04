# POC #3: Scalable RPC

## What It Does

Demonstrates request-response RPC pattern with Apache Pulsar. Client sends requests and waits for responses with timeout handling. Server processes requests and sends results back.

## Key Technologies

- **CompletableFuture with Timeout**: Async request handling with 5-second timeout
- **Virtual Threads**: High-throughput concurrent RPC calls
- **Spring Pulsar**: Integration with Pulsar messaging
- **Shared Subscriptions**: Load distribution across multiple servers

## Architecture

Two-topic RPC pattern:
```
persistent://public/default/rpc-requests   (client -> server)
persistent://public/default/rpc-responses  (server -> client)
```

## Components

**RequestService**:
- Sends RPC requests with unique IDs
- Maintains pending request registry with CompletableFuture
- Handles responses with 5-second timeout
- Demonstrates three operations: CALCULATE, QUERY, TRANSFORM

**ResponseService**:
- Processes incoming requests
- Executes operations with simulated processing time
- Sends results to replyTo topic
- Handles errors gracefully

**RpcRequest**: Request model with requestId, operation, payload, and replyTo

**RpcResponse**: Response model with requestId, result, error, and success flag

## Running

```bash
docker-compose up -d
cd poc3-rpc
mvn spring-boot:run
```

Application runs on port 8083 and executes 5 RPC calls over ~8 seconds.

## Output

Successful RPC flow:
```
Sending RPC request: id=..., operation=CALCULATE
Processing RPC request: id=..., operation=CALCULATE
Sent response for request: id=...
RPC success: id=..., result=Result: 420
```

Timeout handling:
```
Sending RPC request: id=..., operation=QUERY
RPC timeout: id=..., operation=QUERY
```

## RPC Pattern Benefits

- Decoupled request-response communication
- Automatic timeout handling prevents hung requests
- Scalable with multiple server instances
- Asynchronous processing with virtual threads
- Correlation ID tracking for request matching
