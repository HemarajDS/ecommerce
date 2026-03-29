package com.mercora.notification.dto;

import java.time.Instant;

public record DealerEventMessage(
        String eventType,
        String dealerId,
        String referenceId,
        Instant occurredAt) {
}
