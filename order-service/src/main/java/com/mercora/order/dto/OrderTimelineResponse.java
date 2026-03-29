package com.mercora.order.dto;

import com.mercora.order.model.OrderStatus;
import java.time.Instant;

public record OrderTimelineResponse(
        OrderStatus status,
        String note,
        Instant changedAt) {
}
