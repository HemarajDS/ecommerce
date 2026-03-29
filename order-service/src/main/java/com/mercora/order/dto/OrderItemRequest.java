package com.mercora.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String productId,
        @NotBlank String sku,
        @NotBlank String name,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice,
        @NotNull BigDecimal lineTotal) {
}
