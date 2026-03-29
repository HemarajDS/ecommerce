package com.mercora.dealer.dto;

import com.mercora.dealer.model.DealerStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record DealerResponse(
        String id,
        String dealerCode,
        String userId,
        String companyName,
        String contactName,
        String email,
        DealerStatus status,
        BigDecimal creditLimit,
        BigDecimal monthlyLimit,
        int productQuota,
        BigDecimal creditUsed,
        Instant createdAt) {
}
