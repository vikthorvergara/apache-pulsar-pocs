package com.vikthorvergara.pulsar.messaging.consumer;

import com.vikthorvergara.pulsar.messaging.model.TenantEvent;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

@Component
public class TenantEventListener {

    private static final Logger log = LoggerFactory.getLogger(TenantEventListener.class);

    @PulsarListener(
        topics = "persistent://healthcare/events/patient-events",
        subscriptionName = "healthcare-subscription",
        subscriptionType = SubscriptionType.Shared
    )
    public void handleHealthcareEvent(TenantEvent event) {
        log.info("[HEALTHCARE] Received: {}", event);
        processEvent(event);
    }

    @PulsarListener(
        topics = "persistent://finance/events/transaction-events",
        subscriptionName = "finance-subscription",
        subscriptionType = SubscriptionType.Shared
    )
    public void handleFinanceEvent(TenantEvent event) {
        log.info("[FINANCE] Received: {}", event);
        processEvent(event);
    }

    @PulsarListener(
        topics = "persistent://retail/events/order-events",
        subscriptionName = "retail-subscription",
        subscriptionType = SubscriptionType.Shared
    )
    public void handleRetailEvent(TenantEvent event) {
        log.info("[RETAIL] Received: {}", event);
        processEvent(event);
    }

    private void processEvent(TenantEvent event) {
        try {
            Thread.sleep(100);
            log.debug("Processed event {} for tenant {}", event.getEventName(), event.getTenantId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
