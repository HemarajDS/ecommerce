package com.mercora.dealer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PurchaseOrderItemRequest(
        @NotBlank String productId,
        @NotBlank String sku,
        @Min(1) int quantity,
        @NotNull BigDecimal dealerPrice) {
}
