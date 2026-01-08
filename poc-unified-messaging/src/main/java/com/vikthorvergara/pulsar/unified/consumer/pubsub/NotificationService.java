package com.vikthorvergara.pulsar.unified.consumer.pubsub;

import com.vikthorvergara.pulsar.unified.model.Payment;
import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.StructuredTaskScope;

@Component
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String PAYMENT_TOPIC = "persistent://public/default/payments";

    @PulsarListener(
        topics = PAYMENT_TOPIC,
        subscriptionName = "notifications",
        subscriptionType = SubscriptionType.Exclusive
    )
    public void sendNotifications(Payment payment) {
        log.info("[PUB/SUB-NOTIFY] Received payment: id={}, merchant={}, customer={}",
            payment.getId(), payment.getMerchantId(), payment.getCustomerId());

        try (var scope = StructuredTaskScope.open()) {
            var emailTask = scope.fork(() -> sendEmailNotification(payment));
            var smsTask = scope.fork(() -> sendSmsNotification(payment));
            var pushTask = scope.fork(() -> sendPushNotification(payment));

            scope.join();

            emailTask.get();
            smsTask.get();
            pushTask.get();

            log.info("[PUB/SUB-NOTIFY] All notifications sent: id={}, customer={}",
                payment.getId(), payment.getCustomerId());

        } catch (Exception e) {
            log.error("[PUB/SUB-NOTIFY] Notification failed: id={}", payment.getId(), e);
        }
    }

    private Void sendEmailNotification(Payment payment) {
        try {
            Thread.sleep(50);

            log.debug("[NOTIFY] Email sent: customer={}, subject='Payment Confirmation', amount={}",
                payment.getCustomerId(), payment.getAmount());

            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email notification interrupted", e);
        }
    }

    private Void sendSmsNotification(Payment payment) {
        try {
            Thread.sleep(40);

            log.debug("[NOTIFY] SMS sent: customer={}, message='Payment of {} processed'",
                payment.getCustomerId(), payment.getAmount());

            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS notification interrupted", e);
        }
    }

    private Void sendPushNotification(Payment payment) {
        try {
            Thread.sleep(35);

            log.debug("[NOTIFY] Push notification sent: customer={}, title='Payment Successful'",
                payment.getCustomerId());

            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Push notification interrupted", e);
        }
    }
}
