package com.vikthorvergara.pulsar.missioncritical.consumer;

import com.vikthorvergara.pulsar.missioncritical.model.Payment;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.StructuredTaskScope;

@Component
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payment-transactions";

    private final PulsarTemplate pulsarTemplate;

    public PaymentProcessor(PulsarTemplate pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    @PulsarListener(
        topics = PAYMENT_TOPIC,
        subscriptionName = "payment-processor-subscription",
        subscriptionType = SubscriptionType.Failover,
        concurrency = "1"
    )
    public void processPayment(Payment payment) {
        log.info("Processor received payment: id={}, merchant={}, amount={} {}, retry={}",
            payment.getId(), payment.getMerchantId(),
            payment.getAmount(), payment.getCurrency(), payment.getRetryCount());

        payment.setStatus("PROCESSING");
        payment.setLastAttemptTimestamp(Instant.now());

        try (var scope = StructuredTaskScope.open()) {
            var validationTask = scope.fork(() -> validatePayment(payment));
            var processingTask = scope.fork(() -> processTransaction(payment));
            var auditTask = scope.fork(() -> auditPayment(payment));

            scope.join();

            Boolean isValid = validationTask.get();
            String processingResult = processingTask.get();
            auditTask.get();

            if (!isValid) {
                payment.setStatus("REJECTED");
                log.warn("Payment rejected: id={}, reason={}",
                    payment.getId(), payment.getFailureReason());
                return;
            }

            if (processingResult == null || !processingResult.equals("SUCCESS")) {
                handleProcessingFailure(payment);
                return;
            }

            payment.setStatus("COMPLETED");
            payment.setCompletionTimestamp(Instant.now());
            log.info("Payment completed successfully: id={}, merchant={}, amount={} {}",
                payment.getId(), payment.getMerchantId(),
                payment.getAmount(), payment.getCurrency());

        } catch (Exception e) {
            log.error("Payment processing failed: id={}, error={}",
                payment.getId(), e.getMessage());
            handleProcessingFailure(payment);
        }
    }

    private Boolean validatePayment(Payment payment) {
        try {
            log.debug("Validating payment: id={}", payment.getId());
            Thread.sleep(50);

            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                payment.setFailureReason("Invalid amount");
                return false;
            }

            if (payment.getAmount().compareTo(new BigDecimal("10000")) > 0) {
                payment.setFailureReason("Amount exceeds limit");
                return false;
            }

            if (payment.getMerchantId() == null || payment.getCustomerId() == null) {
                payment.setFailureReason("Invalid merchant or customer");
                return false;
            }

            log.debug("Payment validation passed: id={}", payment.getId());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Validation interrupted", e);
        }
    }

    private String processTransaction(Payment payment) {
        try {
            log.debug("Processing payment transaction: id={}, amount={}",
                payment.getId(), payment.getAmount());

            long processingTime = getProcessingTime(payment.getAmount());
            Thread.sleep(processingTime);

            log.debug("Payment transaction completed: id={}", payment.getId());
            return "SUCCESS";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction processing interrupted", e);
        }
    }

    private long getProcessingTime(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("100")) < 0) {
            return 100;
        } else if (amount.compareTo(new BigDecimal("1000")) < 0) {
            return 250;
        } else {
            return 400;
        }
    }

    private Void auditPayment(Payment payment) {
        try {
            log.debug("Auditing payment: id={}", payment.getId());
            Thread.sleep(30);
            log.debug("Payment audit recorded: id={}, merchant={}, amount={}",
                payment.getId(), payment.getMerchantId(), payment.getAmount());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Audit failed for payment: id={}", payment.getId());
            return null;
        }
    }

    private void handleProcessingFailure(Payment payment) {
        payment.setRetryCount(payment.getRetryCount() + 1);

        if (payment.getRetryCount() < payment.getMaxRetries()) {
            log.warn("Payment processing failed, will retry: id={}, retryCount={}/{}",
                payment.getId(), payment.getRetryCount(), payment.getMaxRetries());

            try {
                Thread.sleep(1000);
                pulsarTemplate.send(PAYMENT_TOPIC, payment);
                log.info("Payment republished for retry: id={}, retryCount={}",
                    payment.getId(), payment.getRetryCount());
            } catch (Exception e) {
                log.error("Failed to republish payment: id={}", payment.getId(), e);
            }
        } else {
            payment.setStatus("FAILED");
            payment.setFailureReason("Max retries exceeded");
            log.error("Payment permanently failed: id={}, retryCount={}",
                payment.getId(), payment.getRetryCount());
        }
    }
}
