package com.mercora.order.dto;

import com.mercora.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,
        String orderNumber,
        String userId,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal grandTotal,
        String currency,
        String paymentSessionId,
        String paymentMethod,
        ReturnRequestResponse returnRequest,
        List<OrderTimelineResponse> timeline,
        Instant createdAt,
        Instant updatedAt) {
}
