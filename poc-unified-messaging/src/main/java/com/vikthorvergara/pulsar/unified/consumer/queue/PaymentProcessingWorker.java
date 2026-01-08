package com.vikthorvergara.pulsar.unified.consumer.queue;

import com.vikthorvergara.pulsar.unified.model.Payment;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.StructuredTaskScope;

@Component
public class PaymentProcessingWorker {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingWorker.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";

    @PulsarListener(
        topics = PAYMENT_TOPIC,
        subscriptionName = "payment-processors",
        subscriptionType = SubscriptionType.Shared,
        concurrency = "3"
    )
    public void processPayment(Payment payment) {
        String worker = Thread.currentThread().getName();

        log.info("[QUEUE-WORKER] Worker {} received payment: id={}, merchant={}, amount={} {}",
            worker, payment.getId(), payment.getMerchantId(),
            payment.getAmount(), payment.getCurrency());

        try (var scope = StructuredTaskScope.open()) {
            var validationTask = scope.fork(() -> validatePayment(payment, worker));
            var executionTask = scope.fork(() -> executeTransaction(payment, worker));
            var settlementTask = scope.fork(() -> settlePayment(payment, worker));

            scope.join();

            Boolean isValid = validationTask.get();
            String txnId = executionTask.get();
            settlementTask.get();

            if (!isValid) {
                log.warn("[QUEUE-WORKER] Worker {} rejected invalid payment: id={}",
                    worker, payment.getId());
                return;
            }

            log.info("[QUEUE-WORKER] Worker {} completed payment: id={}, transaction={}",
                worker, payment.getId(), txnId);

        } catch (Exception e) {
            log.error("[QUEUE-WORKER] Worker {} failed to process payment: id={}",
                worker, payment.getId(), e);
        }
    }

    private Boolean validatePayment(Payment payment, String worker) {
        try {
            Thread.sleep(60);

            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("[QUEUE] Worker {} validation failed: invalid amount", worker);
                return false;
            }

            if (payment.getMerchantId() == null || payment.getCustomerId() == null) {
                log.debug("[QUEUE] Worker {} validation failed: missing IDs", worker);
                return false;
            }

            log.debug("[QUEUE] Worker {} validation passed: id={}", worker, payment.getId());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Validation interrupted", e);
        }
    }

    private String executeTransaction(Payment payment, String worker) {
        try {
            long processingTime = getProcessingTime(payment.getAmount());
            Thread.sleep(processingTime);

            String txnId = "TXN-" + payment.getId().substring(0, 8);

            log.debug("[QUEUE] Worker {} executed transaction: id={}, txn={}",
                worker, payment.getId(), txnId);

            return txnId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction execution interrupted", e);
        }
    }

    private long getProcessingTime(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("100")) < 0) {
            return 100;
        } else if (amount.compareTo(new BigDecimal("1000")) < 0) {
            return 200;
        } else {
            return 300;
        }
    }

    private Void settlePayment(Payment payment, String worker) {
        try {
            Thread.sleep(40);

            log.debug("[QUEUE] Worker {} settlement completed: id={}, merchant={}",
                worker, payment.getId(), payment.getMerchantId());

            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Settlement interrupted", e);
        }
    }
}
