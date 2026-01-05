package com.vikthorvergara.pulsar.missioncritical.producer;

import com.vikthorvergara.pulsar.missioncritical.model.Payment;
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
    private static final String PAYMENT_TOPIC = "persistent://public/default/payment-transactions";

    private final PulsarTemplate pulsarTemplate;

    public PaymentProducer(PulsarTemplate pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(2000);

        var executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 1; i <= 15; i++) {
            int paymentNum = i;
            executor.submit(() -> producePayment(paymentNum));
            Thread.sleep(300);
        }

        Thread.sleep(7000);
        executor.shutdown();
    }

    private void producePayment(int paymentNum) {
        String merchantId = switch (paymentNum % 3) {
            case 0 -> "RETAIL-001";
            case 1 -> "FINANCE-002";
            default -> "HEALTHCARE-003";
        };

        String customerId = "CUST-" + String.format("%04d", paymentNum);

        BigDecimal amount = switch (paymentNum % 5) {
            case 0 -> new BigDecimal("5000.00");
            case 1 -> new BigDecimal("150.50");
            case 2 -> new BigDecimal("75.25");
            case 3 -> new BigDecimal("1250.00");
            default -> new BigDecimal("10.00");
        };

        String description = "Payment for order #" + paymentNum;

        var payment = new Payment(merchantId, customerId, amount, description);

        try {
            pulsarTemplate.send(PAYMENT_TOPIC, payment);
            log.info("Produced payment: merchant={}, customer={}, amount={} {}",
                payment.getMerchantId(), payment.getCustomerId(),
                payment.getAmount(), payment.getCurrency());
        } catch (Exception e) {
            log.error("Failed to produce payment", e);
        }
    }
}
