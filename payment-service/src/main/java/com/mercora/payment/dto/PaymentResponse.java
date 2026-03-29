package com.mercora.payment.dto;

import com.mercora.payment.model.PaymentProvider;
import com.mercora.payment.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String paymentSessionId,
        String orderId,
        String userId,
        PaymentProvider provider,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String gatewayReference,
        Instant createdAt,
        Instant updatedAt) {
}
