package com.mercora.inventory.dto;

import java.time.Instant;
import java.util.List;

public record InventoryItemResponse(
        String id,
        String productId,
        String sku,
        String warehouseCode,
        int availableQuantity,
        int reservedQuantity,
        int safetyStock,
        List<InventoryBatchResponse> batches,
        Instant updatedAt) {
}
