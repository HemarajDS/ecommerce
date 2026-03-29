package com.mercora.inventory.dto;

import java.time.Instant;

public record InventoryBatchRequest(
        String batchCode,
        int quantity,
        Instant expiresAt) {
}
