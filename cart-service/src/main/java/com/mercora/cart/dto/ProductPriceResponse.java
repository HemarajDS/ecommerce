package com.mercora.cart.dto;

import java.math.BigDecimal;

public record ProductPriceResponse(
        String productId,
        String sku,
        String name,
        BigDecimal retailPrice) {
}
