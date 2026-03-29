package com.mercora.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String color,
        String size,
        @NotNull @DecimalMin("0.0") BigDecimal retailPrice,
        @NotNull @DecimalMin("0.0") BigDecimal dealerPrice,
        Integer stockOnHand,
        Map<String, Object> attributes) {
}
