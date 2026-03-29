package com.mercora.inventory.event;

public record LowStockEvent(
        String productId,
        String sku,
        String warehouseCode,
        int availableQuantity,
        int safetyStock) {
}
