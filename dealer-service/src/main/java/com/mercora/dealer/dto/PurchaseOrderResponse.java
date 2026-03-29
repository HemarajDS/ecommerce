package com.mercora.dealer.dto;

import com.mercora.dealer.model.PurchaseOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseOrderResponse(
        String id,
        String dealerId,
        String poNumber,
        PurchaseOrderStatus status,
        List<PurchaseOrderItemResponse> items,
        BigDecimal totalAmount,
        String approvalReason,
        Instant dueDate,
        Instant createdAt) {
}
