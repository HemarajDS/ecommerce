package com.mercora.dealer.dto;

import com.mercora.dealer.model.LedgerEntryType;
import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(
        String id,
        String dealerId,
        String purchaseOrderId,
        LedgerEntryType type,
        BigDecimal amount,
        String description,
        Instant createdAt) {
}
