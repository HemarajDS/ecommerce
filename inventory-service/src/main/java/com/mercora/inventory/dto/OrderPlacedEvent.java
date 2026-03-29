package com.mercora.inventory.dto;

public record OrderPlacedEvent(
        String orderId,
        String productId,
        String sku,
        String warehouseCode,
        int quantity) {
}
