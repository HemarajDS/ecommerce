package com.mercora.product.dto;

import com.mercora.product.model.ProductStatus;
import java.math.BigDecimal;

public record ProductSearchRequest(
        String query,
        String categoryId,
        String brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Double minRating,
        ProductStatus status) {
}
