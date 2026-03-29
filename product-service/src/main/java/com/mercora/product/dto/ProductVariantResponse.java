package com.mercora.product.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantResponse(
        String sku,
        String name,
        String color,
        String size,
        BigDecimal retailPrice,
        BigDecimal dealerPrice,
        Integer stockOnHand,
        Map<String, Object> attributes) {
}
