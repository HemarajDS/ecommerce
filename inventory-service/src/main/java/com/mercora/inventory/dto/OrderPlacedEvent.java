package com.mercora.inventory.dto;

import java.time.Instant;
import java.util.List;

public record OrderPlacedEvent(
        String eventType,
        String orderId,
        String orderNumber,
        String userId,
        String status,
        List<OrderPlacedItem> items,
        Instant occurredAt) {
}
