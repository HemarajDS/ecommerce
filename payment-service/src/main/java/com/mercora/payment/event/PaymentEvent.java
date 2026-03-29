package com.mercora.payment.event;

import com.mercora.payment.model.PaymentStatus;
import java.time.Instant;

public record PaymentEvent(
        String eventType,
        String paymentSessionId,
        String orderId,
        String status,
        Instant occurredAt) {
}
