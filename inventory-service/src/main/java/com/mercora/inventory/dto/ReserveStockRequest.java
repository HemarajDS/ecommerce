package com.mercora.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveStockRequest(
        @NotBlank String orderId,
        @NotBlank String productId,
        @NotBlank String sku,
        @NotBlank String warehouseCode,
        @Min(1) int quantity) {
}
