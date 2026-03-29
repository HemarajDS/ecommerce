package com.mercora.order.event;

import com.mercora.order.model.OrderStatus;
import java.time.Instant;
import java.util.List;

public record OrderEvent(
        String eventType,
        String orderId,
        String orderNumber,
        String userId,
        String status,
        List<OrderEventItem> items,
        Instant occurredAt) {
}
