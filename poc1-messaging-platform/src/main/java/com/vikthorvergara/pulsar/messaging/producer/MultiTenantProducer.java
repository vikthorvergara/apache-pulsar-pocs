package com.vikthorvergara.pulsar.messaging.producer;

import com.vikthorvergara.pulsar.messaging.model.TenantEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class MultiTenantProducer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MultiTenantProducer.class);
    private static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    private final PulsarTemplate<TenantEvent> pulsarTemplate;

    public MultiTenantProducer(PulsarTemplate<TenantEvent> pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(2000);

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        executor.submit(() -> publishHealthcareEvents());
        executor.submit(() -> publishFinanceEvents());
        executor.submit(() -> publishRetailEvents());

        Thread.sleep(5000);
        executor.shutdown();
    }

    private void publishHealthcareEvents() {
        ScopedValue.runWhere(TENANT_ID, "healthcare", () -> {
            for (int i = 1; i <= 5; i++) {
                var event = new TenantEvent(
                    getTenantId(),
                    "PatientRegistered",
                    "Patient P" + i + " registered"
                );
                sendEvent("persistent://healthcare/events/patient-events", event);
                sleep(500);
            }
        });
    }

    private void publishFinanceEvents() {
        ScopedValue.runWhere(TENANT_ID, "finance", () -> {
            for (int i = 1; i <= 5; i++) {
                var event = new TenantEvent(
                    getTenantId(),
                    "TransactionCreated",
                    "Transaction T" + i + " created for $" + (i * 100)
                );
                sendEvent("persistent://finance/events/transaction-events", event);
                sleep(500);
            }
        });
    }

    private void publishRetailEvents() {
        ScopedValue.runWhere(TENANT_ID, "retail", () -> {
            for (int i = 1; i <= 5; i++) {
                var event = new TenantEvent(
                    getTenantId(),
                    "OrderPlaced",
                    "Order O" + i + " placed with " + (i + 2) + " items"
                );
                sendEvent("persistent://retail/events/order-events", event);
                sleep(500);
            }
        });
    }

    private void sendEvent(String topic, TenantEvent event) {
        try {
            pulsarTemplate.send(topic, event);
            log.info("Published event to {}: {}", topic, event);
        } catch (Exception e) {
            log.error("Failed to publish event", e);
        }
    }

    private String getTenantId() {
        return TENANT_ID.get();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
