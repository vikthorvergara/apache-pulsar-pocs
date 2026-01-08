package com.vikthorvergara.pulsar.unified.consumer.streaming;

import com.vikthorvergara.pulsar.unified.model.Payment;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@Component
public class AuditService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";

    private final PulsarClient pulsarClient;

    public AuditService(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);

        log.info("[STREAMING-AUDIT] Starting audit reader to replay payment history");

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        executor.submit(() -> {
            try {
                Reader<Payment> reader = pulsarClient.newReader(Schema.JSON(Payment.class))
                    .topic(PAYMENT_TOPIC)
                    .startMessageId(org.apache.pulsar.client.api.MessageId.earliest)
                    .readerName("audit-reader")
                    .create();

                List<Payment> auditTrail = new ArrayList<>();
                Map<String, Integer> merchantCounts = new HashMap<>();

                while (reader.hasMessageAvailable()) {
                    Message<Payment> message = reader.readNext();
                    Payment payment = message.getValue();

                    auditTrail.add(payment);

                    merchantCounts.merge(
                        payment.getMerchantId(),
                        1,
                        Integer::sum
                    );

                    log.debug("[AUDIT] Replayed payment: id={}, merchant={}, amount={}, timestamp={}",
                        payment.getId(), payment.getMerchantId(),
                        payment.getAmount(), payment.getTimestamp());
                }

                generateAuditReport(auditTrail, merchantCounts);

                Thread.sleep(2000);
                generateAuditReport(auditTrail, merchantCounts);

            } catch (Exception e) {
                log.error("[STREAMING-AUDIT] Audit reader failed", e);
            }
        });
    }

    private void generateAuditReport(List<Payment> auditTrail, Map<String, Integer> merchantCounts) {
        log.info("[STREAMING-AUDIT] Audit Report Generated:");
        log.info("[STREAMING-AUDIT]   Total Payments Audited: {}", auditTrail.size());
        log.info("[STREAMING-AUDIT]   Merchant Transaction Counts:");

        merchantCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry ->
                log.info("[STREAMING-AUDIT]     {} -> {} transactions",
                    entry.getKey(), entry.getValue())
            );

        long complianceIssues = auditTrail.stream()
            .filter(p -> p.getAmount().compareTo(new java.math.BigDecimal("5000")) > 0)
            .count();

        log.info("[STREAMING-AUDIT]   High-Value Payments (>$5000): {}", complianceIssues);
        log.info("[STREAMING-AUDIT] Audit report complete - replay capability demonstrated");
    }
}
