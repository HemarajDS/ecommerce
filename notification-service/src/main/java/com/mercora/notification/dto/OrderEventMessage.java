package com.mercora.notification.dto;

import java.time.Instant;

public record OrderEventMessage(
        String eventType,
        String orderId,
        String orderNumber,
        String userId,
        String status,
        Instant occurredAt) {
}
