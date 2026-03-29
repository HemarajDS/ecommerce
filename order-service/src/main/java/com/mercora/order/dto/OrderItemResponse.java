package com.mercora.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String sku,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
