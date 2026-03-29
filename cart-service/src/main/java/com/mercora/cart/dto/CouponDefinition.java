package com.mercora.cart.dto;

import com.mercora.cart.model.CouponType;
import java.math.BigDecimal;

public record CouponDefinition(
        String code,
        CouponType type,
        BigDecimal value,
        boolean active) {
}
