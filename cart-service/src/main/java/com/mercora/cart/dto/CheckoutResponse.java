package com.mercora.cart.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CheckoutResponse(
        String paymentSessionId,
        String paymentMethod,
        String currency,
        BigDecimal amount,
        Instant expiresAt) {
}
