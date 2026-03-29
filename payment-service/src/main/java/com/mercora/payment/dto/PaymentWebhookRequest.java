package com.mercora.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentWebhookRequest(
        @NotBlank String paymentSessionId,
        @NotBlank String gatewayReference,
        @NotBlank String status,
        @NotBlank String idempotencyKey) {
}
