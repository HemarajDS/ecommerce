package com.mercora.product.event;

import com.mercora.product.model.ProductStatus;
import java.time.Instant;

public record ProductEvent(
        String eventType,
        String productId,
        String slug,
        String name,
        String brandId,
        String categoryId,
        ProductStatus status,
        Instant occurredAt) {
}
