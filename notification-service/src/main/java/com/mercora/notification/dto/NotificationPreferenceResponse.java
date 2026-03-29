package com.mercora.notification.dto;

import java.time.Instant;

public record NotificationPreferenceResponse(
        String userId,
        boolean emailEnabled,
        boolean smsEnabled,
        boolean inAppEnabled,
        Instant updatedAt) {
}
