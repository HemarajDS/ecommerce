package com.mercora.notification.dto;

import java.time.Instant;

public record PaymentEventMessage(
        String eventType,
        String paymentSessionId,
        String orderId,
        String status,
        Instant occurredAt) {
}
