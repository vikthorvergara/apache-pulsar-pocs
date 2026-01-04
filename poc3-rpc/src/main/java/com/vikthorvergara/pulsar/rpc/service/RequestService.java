package com.vikthorvergara.pulsar.rpc.service;

import com.vikthorvergara.pulsar.rpc.model.RpcRequest;
import com.vikthorvergara.pulsar.rpc.model.RpcResponse;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class RequestService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RequestService.class);
    private static final String REQUEST_TOPIC = "persistent://public/default/rpc-requests";
    private static final String RESPONSE_TOPIC = "persistent://public/default/rpc-responses";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final PulsarTemplate pulsarTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<RpcResponse>> pendingRequests;

    public RequestService(PulsarTemplate pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(2000);

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        executor.submit(() -> sendRequest("CALCULATE", "test-data-1"));
        Thread.sleep(100);
        executor.submit(() -> sendRequest("QUERY", "user-info"));
        Thread.sleep(100);
        executor.submit(() -> sendRequest("TRANSFORM", "lowercase text"));
        Thread.sleep(100);
        executor.submit(() -> sendRequest("CALCULATE", "test-data-2"));
        Thread.sleep(100);
        executor.submit(() -> sendRequest("QUERY", "order-details"));

        Thread.sleep(8000);
        executor.shutdown();
    }

    @PulsarListener(
        topics = RESPONSE_TOPIC,
        subscriptionName = "rpc-client-subscription",
        subscriptionType = SubscriptionType.Shared
    )
    public void handleResponse(RpcResponse response) {
        log.debug("Received response for request: id={}", response.getRequestId());

        var future = pendingRequests.remove(response.getRequestId());
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("No pending request found for response: id={}", response.getRequestId());
        }
    }

    private void sendRequest(String operation, String payload) {
        String requestId = UUID.randomUUID().toString();

        var request = new RpcRequest(requestId, operation, payload, RESPONSE_TOPIC);

        var responseFuture = new CompletableFuture<RpcResponse>();
        pendingRequests.put(requestId, responseFuture);

        try {
            log.info("Sending RPC request: id={}, operation={}", requestId, operation);
            pulsarTemplate.send(REQUEST_TOPIC, request);

            var response = responseFuture.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            if (response.isSuccess()) {
                log.info("RPC success: id={}, result={}", requestId, response.getResult());
            } else {
                log.error("RPC failed: id={}, error={}", requestId, response.getError());
            }

        } catch (TimeoutException e) {
            pendingRequests.remove(requestId);
            log.error("RPC timeout: id={}, operation={}", requestId, operation);

        } catch (Exception e) {
            pendingRequests.remove(requestId);
            log.error("RPC error: id={}, operation={}, error={}",
                requestId, operation, e.getMessage());
        }
    }
}
