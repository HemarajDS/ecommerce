package com.mercora.dealer.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        String productId,
        String sku,
        int quantity,
        BigDecimal dealerPrice,
        BigDecimal lineTotal) {
}
