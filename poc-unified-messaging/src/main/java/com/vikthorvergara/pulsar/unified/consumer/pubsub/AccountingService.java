package com.vikthorvergara.pulsar.unified.consumer.pubsub;

import com.vikthorvergara.pulsar.unified.model.Payment;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.StructuredTaskScope;

@Component
public class AccountingService {

    private static final Logger log = LoggerFactory.getLogger(AccountingService.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    @PulsarListener(
        topics = PAYMENT_TOPIC,
        subscriptionName = "accounting",
        subscriptionType = SubscriptionType.Exclusive
    )
    public void recordPayment(Payment payment) {
        log.info("[PUB/SUB-ACCOUNTING] Received payment: id={}, merchant={}, amount={} {}",
            payment.getId(), payment.getMerchantId(), payment.getAmount(), payment.getCurrency());

        try (var scope = StructuredTaskScope.open()) {
            var ledgerTask = scope.fork(() -> createLedgerEntry(payment));
            var taxTask = scope.fork(() -> calculateTax(payment));
            var reportTask = scope.fork(() -> updateFinancialReport(payment));

            scope.join();

            String ledgerId = ledgerTask.get();
            BigDecimal tax = taxTask.get();
            reportTask.get();

            BigDecimal netAmount = payment.getAmount().subtract(tax);

            log.info("[PUB/SUB-ACCOUNTING] Accounting completed: id={}, ledger={}, tax={}, net={}",
                payment.getId(), ledgerId, tax, netAmount);

        } catch (Exception e) {
            log.error("[PUB/SUB-ACCOUNTING] Accounting failed: id={}", payment.getId(), e);
        }
    }

    private String createLedgerEntry(Payment payment) {
        try {
            Thread.sleep(45);

            String ledgerId = "LGR-" + payment.getId().substring(0, 8);

            log.debug("[ACCOUNTING] Ledger entry created: ledger={}, payment={}, amount={}",
                ledgerId, payment.getId(), payment.getAmount());

            return ledgerId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ledger creation interrupted", e);
        }
    }

    private BigDecimal calculateTax(Payment payment) {
        try {
            Thread.sleep(30);

            BigDecimal tax = payment.getAmount()
                .multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

            log.debug("[ACCOUNTING] Tax calculated: payment={}, amount={}, tax={}",
                payment.getId(), payment.getAmount(), tax);

            return tax;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Tax calculation interrupted", e);
        }
    }

    private Void updateFinancialReport(Payment payment) {
        try {
            Thread.sleep(25);

            log.debug("[ACCOUNTING] Financial report updated: payment={}, merchant={}",
                payment.getId(), payment.getMerchantId());

            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Report update interrupted", e);
        }
    }
}
