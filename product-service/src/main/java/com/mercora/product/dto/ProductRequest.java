package com.mercora.product.dto;

import com.mercora.product.model.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String brandId,
        @NotBlank String categoryId,
        List<String> tags,
        @NotNull @DecimalMin("0.0") BigDecimal retailPrice,
        @NotNull @DecimalMin("0.0") BigDecimal dealerPrice,
        Double rating,
        @NotNull ProductStatus status,
        List<String> imageUrls,
        Map<String, Object> attributes,
        @Valid @NotEmpty List<ProductVariantRequest> variants) {
}
