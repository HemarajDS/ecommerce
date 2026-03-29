package com.mercora.order.event;

public record OrderEventItem(
        String productId,
        String sku,
        int quantity,
        String warehouseCode) {
}
