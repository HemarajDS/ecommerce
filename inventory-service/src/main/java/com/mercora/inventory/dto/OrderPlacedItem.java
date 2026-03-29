package com.mercora.inventory.dto;

public record OrderPlacedItem(
        String productId,
        String sku,
        int quantity,
        String warehouseCode) {
}
