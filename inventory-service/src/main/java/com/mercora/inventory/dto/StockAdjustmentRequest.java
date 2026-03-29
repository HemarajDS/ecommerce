package com.mercora.inventory.dto;

import com.mercora.inventory.model.StockAdjustmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record StockAdjustmentRequest(
        @NotBlank String productId,
        @NotBlank String sku,
        @NotBlank String warehouseCode,
        @Min(0) Integer safetyStock,
        @NotNull StockAdjustmentType type,
        @NotBlank String reason,
        @Valid @NotEmpty List<InventoryBatchRequest> batches) {
}
