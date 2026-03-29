package com.mercora.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @Valid @NotEmpty List<OrderItemRequest> items,
        @NotNull BigDecimal subtotal,
        @NotNull BigDecimal discountTotal,
        @NotNull BigDecimal grandTotal,
        @NotBlank String currency,
        @NotBlank String paymentSessionId,
        @NotBlank String paymentMethod) {
}
