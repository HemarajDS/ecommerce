package com.mercora.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank String orderId,
        @NotBlank String userId,
        @NotBlank String paymentSessionId,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String idempotencyKey) {
}
