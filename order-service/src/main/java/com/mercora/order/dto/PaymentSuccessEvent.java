package com.mercora.order.dto;

public record PaymentSuccessEvent(
        String paymentSessionId,
        String orderId,
        String status) {
}
