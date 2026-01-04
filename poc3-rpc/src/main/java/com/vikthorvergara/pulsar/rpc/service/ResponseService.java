package com.vikthorvergara.pulsar.rpc.service;

import com.vikthorvergara.pulsar.rpc.model.RpcRequest;
import com.vikthorvergara.pulsar.rpc.model.RpcResponse;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    private static final Logger log = LoggerFactory.getLogger(ResponseService.class);

    private final PulsarTemplate pulsarTemplate;

    public ResponseService(PulsarTemplate pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    @PulsarListener(
        topics = "persistent://public/default/rpc-requests",
        subscriptionName = "rpc-server-subscription",
        subscriptionType = SubscriptionType.Shared,
        concurrency = "2"
    )
    public void handleRequest(RpcRequest request) {
        log.info("Processing RPC request: id={}, operation={}",
            request.getRequestId(), request.getOperation());

        try {
            String result = processOperation(request);

            var response = new RpcResponse(request.getRequestId(), result);

            pulsarTemplate.send(request.getReplyTo(), response);

            log.info("Sent response for request: id={}", request.getRequestId());

        } catch (Exception e) {
            log.error("Failed to process request: id={}, error={}",
                request.getRequestId(), e.getMessage());

            var errorResponse = new RpcResponse(
                request.getRequestId(),
                "Error: " + e.getMessage(),
                false
            );

            try {
                pulsarTemplate.send(request.getReplyTo(), errorResponse);
            } catch (Exception sendError) {
                log.error("Failed to send error response", sendError);
            }
        }
    }

    private String processOperation(RpcRequest request) throws InterruptedException {
        long processingTime = switch (request.getOperation()) {
            case "CALCULATE" -> 200;
            case "QUERY" -> 150;
            case "TRANSFORM" -> 300;
            default -> 100;
        };

        Thread.sleep(processingTime);

        return switch (request.getOperation()) {
            case "CALCULATE" -> "Result: " + (request.getPayload().length() * 42);
            case "QUERY" -> "Data: [" + request.getPayload() + "]";
            case "TRANSFORM" -> request.getPayload().toUpperCase();
            default -> "Processed: " + request.getPayload();
        };
    }
}
