package com.mercora.cart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponse(
        String userId,
        List<CartItemResponse> items,
        CouponResponse coupon,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal grandTotal,
        Instant updatedAt,
        Instant expiresAt) {
}
