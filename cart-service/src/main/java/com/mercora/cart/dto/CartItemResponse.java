package com.mercora.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        String productId,
        String sku,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
