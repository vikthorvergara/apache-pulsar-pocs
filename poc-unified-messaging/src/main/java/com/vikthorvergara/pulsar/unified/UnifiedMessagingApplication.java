package com.vikthorvergara.pulsar.unified;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.vikthorvergara.pulsar.unified",
    "com.vikthorvergara.pulsar.common"
})
public class UnifiedMessagingApplication {

    private static final Logger log = LoggerFactory.getLogger(UnifiedMessagingApplication.class);

    public static void main(String[] args) {
        log.info("Starting Unified Messaging Platform - POC5");
        log.info("Demonstrating Pub/Sub + Queueing + Streaming patterns on single topic");

        SpringApplication.run(UnifiedMessagingApplication.class, args);
    }
}
