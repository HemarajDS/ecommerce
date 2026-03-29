package com.mercora.dealer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DealerOnboardingRequest(
        @NotBlank String userId,
        @NotBlank String companyName,
        @NotBlank String contactName,
        @NotBlank String email,
        @NotNull BigDecimal creditLimit,
        @NotNull BigDecimal monthlyLimit,
        int productQuota) {
}
