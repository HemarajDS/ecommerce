package com.mercora.inventory.dto;

import java.time.Instant;

public record InventoryBatchResponse(
        String batchCode,
        int availableQuantity,
        int reservedQuantity,
        Instant expiresAt) {
}
