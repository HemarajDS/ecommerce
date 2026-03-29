package com.mercora.order.dto;

import com.mercora.order.model.ReturnStatus;
import java.time.Instant;

public record ReturnRequestResponse(
        ReturnStatus status,
        String reason,
        Instant requestedAt,
        Instant updatedAt) {
}
