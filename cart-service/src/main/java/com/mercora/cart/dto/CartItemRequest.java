package com.mercora.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CartItemRequest(
        @NotBlank String productId,
        @NotBlank String sku,
        @Min(1) int quantity) {
}
