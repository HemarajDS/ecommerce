package com.mercora.cart.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        @NotBlank String paymentMethod,
        @NotBlank String currency,
        String notes) {
}
