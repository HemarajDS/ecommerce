package com.mercora.notification.dto;

import com.mercora.notification.model.NotificationChannel;
import com.mercora.notification.model.NotificationStatus;
import java.time.Instant;

public record NotificationHistoryResponse(
        String id,
        String userId,
        String subject,
        String message,
        NotificationChannel channel,
        NotificationStatus status,
        String eventType,
        Instant createdAt) {
}
