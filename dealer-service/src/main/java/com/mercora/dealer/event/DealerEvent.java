package com.mercora.dealer.event;

import java.time.Instant;

public record DealerEvent(
        String eventType,
        String dealerId,
        String referenceId,
        Instant occurredAt) {
}
