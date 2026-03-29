package com.mercora.product.dto;

import com.mercora.product.model.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProductResponse(
        String id,
        String slug,
        String name,
        String description,
        String brandId,
        String categoryId,
        List<String> tags,
        BigDecimal retailPrice,
        BigDecimal dealerPrice,
        Double rating,
        ProductStatus status,
        List<String> imageUrls,
        Map<String, Object> attributes,
        List<ProductVariantResponse> variants,
        Instant createdAt,
        Instant updatedAt) {
}
