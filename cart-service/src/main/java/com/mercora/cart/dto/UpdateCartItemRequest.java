package com.mercora.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateCartItemRequest(
        @NotBlank String sku,
        @Min(1) int quantity) {
}
