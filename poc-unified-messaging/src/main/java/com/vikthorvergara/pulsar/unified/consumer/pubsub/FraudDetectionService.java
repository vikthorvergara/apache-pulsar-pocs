package com.vikthorvergara.pulsar.unified.consumer.pubsub;

import com.vikthorvergara.pulsar.unified.model.Payment;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.StructuredTaskScope;

@Component
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";

    @PulsarListener(
        topics = PAYMENT_TOPIC,
        subscriptionName = "fraud-detection",
        subscriptionType = SubscriptionType.Exclusive
    )
    public void detectFraud(Payment payment) {
        log.info("[PUB/SUB-FRAUD] Received payment: id={}, merchant={}, amount={} {}",
            payment.getId(), payment.getMerchantId(), payment.getAmount(), payment.getCurrency());

        try (var scope = StructuredTaskScope.open()) {
            var amountCheck = scope.fork(() -> validateAmount(payment));
            var merchantCheck = scope.fork(() -> verifyMerchant(payment));
            var historyCheck = scope.fork(() -> checkCustomerHistory(payment));

            scope.join();

            int amountScore = amountCheck.get();
            int merchantScore = merchantCheck.get();
            int historyScore = historyCheck.get();

            int totalScore = amountScore + merchantScore + historyScore;

            String decision = totalScore >= 70 ? "APPROVED" : "FLAGGED";

            log.info("[PUB/SUB-FRAUD] Fraud check completed: id={}, score={}, decision={}",
                payment.getId(), totalScore, decision);

        } catch (Exception e) {
            log.error("[PUB/SUB-FRAUD] Fraud detection failed: id={}", payment.getId(), e);
        }
    }

    private Integer validateAmount(Payment payment) {
        try {
            Thread.sleep(40);

            if (payment.getAmount().compareTo(new BigDecimal("5000")) > 0) {
                log.debug("[FRAUD] High amount detected: id={}, amount={}",
                    payment.getId(), payment.getAmount());
                return 30;
            } else if (payment.getAmount().compareTo(new BigDecimal("1000")) > 0) {
                return 50;
            } else {
                return 100;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Amount validation interrupted", e);
        }
    }

    private Integer verifyMerchant(Payment payment) {
        try {
            Thread.sleep(35);

            String merchantId = payment.getMerchantId();
            if (merchantId.startsWith("TECH")) {
                return 100;
            } else if (merchantId.startsWith("RETAIL")) {
                return 80;
            } else {
                return 90;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Merchant verification interrupted", e);
        }
    }

    private Integer checkCustomerHistory(Payment payment) {
        try {
            Thread.sleep(30);

            int customerNum = Integer.parseInt(payment.getCustomerId().substring(5));

            if (customerNum % 7 == 0) {
                log.debug("[FRAUD] Suspicious customer history: id={}, customer={}",
                    payment.getId(), payment.getCustomerId());
                return 40;
            } else {
                return 100;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("History check interrupted", e);
        }
    }
}
