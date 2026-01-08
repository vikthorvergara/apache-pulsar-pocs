package com.vikthorvergara.pulsar.unified.producer;

import com.vikthorvergara.pulsar.unified.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.Executors;

@Component
public class PaymentProducer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PaymentProducer.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";

    private final PulsarTemplate<Payment> pulsarTemplate;

    public PaymentProducer(PulsarTemplate<Payment> pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(2000);

        log.info("Starting payment production to unified topic: {}", PAYMENT_TOPIC);

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 1; i <= 20; i++) {
            int paymentNum = i;
            executor.submit(() -> producePayment(paymentNum));
            Thread.sleep(300);
        }

        Thread.sleep(5000);
        executor.shutdown();

        log.info("Payment production completed");
    }

    private void producePayment(int paymentNum) {
        String merchantId = switch (paymentNum % 4) {
            case 0 -> "RETAIL-001";
            case 1 -> "FINANCE-002";
            case 2 -> "HEALTHCARE-003";
            default -> "TECH-004";
        };

        String customerId = "CUST-" + String.format("%04d", paymentNum);

        BigDecimal amount = switch (paymentNum % 6) {
            case 0 -> new BigDecimal("7500.00");
            case 1 -> new BigDecimal("150.50");
            case 2 -> new BigDecimal("75.25");
            case 3 -> new BigDecimal("2250.00");
            case 4 -> new BigDecimal("10.00");
            default -> new BigDecimal("500.00");
        };

        String description = "Payment for order #" + paymentNum;

        var payment = new Payment(merchantId, customerId, amount, description);

        try {
            pulsarTemplate.send(PAYMENT_TOPIC, payment);
            log.info("Produced payment #{}: merchant={}, customer={}, amount={} {}",
                paymentNum, payment.getMerchantId(), payment.getCustomerId(),
                payment.getAmount(), payment.getCurrency());
        } catch (Exception e) {
            log.error("Failed to produce payment", e);
        }
    }
}
