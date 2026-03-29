package com.mercora.order.event;

import com.mercora.order.model.OrderStatus;
import java.time.Instant;

public record OrderEvent(
        String eventType,
        String orderId,
        String orderNumber,
        String userId,
        OrderStatus status,
        Instant occurredAt) {
}
