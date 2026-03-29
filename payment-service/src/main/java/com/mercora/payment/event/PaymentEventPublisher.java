package com.mercora.payment.event;

import com.mercora.payment.config.PaymentProperties;
import com.mercora.payment.model.PaymentDocument;
import com.mercora.payment.model.PaymentStatus;
import java.time.Instant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final PaymentProperties paymentProperties;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentEvent> kafkaTemplate, PaymentProperties paymentProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentProperties = paymentProperties;
    }

    public void publish(PaymentDocument payment) {
        String topic = switch (payment.getStatus()) {
            case SUCCESS -> paymentProperties.getPaymentSuccessTopic();
            case FAILED -> paymentProperties.getPaymentFailedTopic();
            case REFUNDED -> paymentProperties.getPaymentRefundedTopic();
            default -> null;
        };
        if (topic != null) {
            String eventType = "payment." + payment.getStatus().name().toLowerCase();
            kafkaTemplate.send(topic, payment.getPaymentSessionId(), new PaymentEvent(
                    eventType,
                    payment.getPaymentSessionId(),
                    payment.getOrderId(),
                    payment.getStatus().name(),
                    Instant.now()));
        }
    }
}
