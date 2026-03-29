package com.mercora.notification.dto;

public record LowStockEventMessage(
        String productId,
        String sku,
        String warehouseCode,
        int availableQuantity,
        int safetyStock) {
}
