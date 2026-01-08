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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

@Component
public class AnalyticsService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";

    private final PulsarClient pulsarClient;

    private int paymentCount = 0;
    private BigDecimal totalVolume = BigDecimal.ZERO;
    private final Map<String, BigDecimal> merchantVolumes = new HashMap<>();

    public AnalyticsService(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(3000);

        log.info("[STREAMING-ANALYTICS] Starting analytics reader from beginning of topic");

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        executor.submit(() -> {
            try {
                Reader<Payment> reader = pulsarClient.newReader(Schema.JSON(Payment.class))
                    .topic(PAYMENT_TOPIC)
                    .startMessageId(org.apache.pulsar.client.api.MessageId.earliest)
                    .readerName("analytics-reader")
                    .create();

                while (true) {
                    Message<Payment> message = reader.readNext();
                    Payment payment = message.getValue();

                    processAnalytics(payment);

                    if (paymentCount % 5 == 0) {
                        logAnalyticsSummary();
                    }
                }

            } catch (Exception e) {
                log.error("[STREAMING-ANALYTICS] Reader failed", e);
            }
        });
    }

    private void processAnalytics(Payment payment) {
        paymentCount++;
        totalVolume = totalVolume.add(payment.getAmount());

        merchantVolumes.merge(
            payment.getMerchantId(),
            payment.getAmount(),
            BigDecimal::add
        );

        log.debug("[ANALYTICS] Processed payment: id={}, count={}, total={}",
            payment.getId(), paymentCount, totalVolume);
    }

    private void logAnalyticsSummary() {
        BigDecimal avgAmount = paymentCount > 0
            ? totalVolume.divide(BigDecimal.valueOf(paymentCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        String topMerchant = merchantVolumes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");

        log.info("[STREAMING-ANALYTICS] Summary: payments={}, totalVolume={}, avgAmount={}, topMerchant={}",
            paymentCount, totalVolume, avgAmount, topMerchant);
    }
}
