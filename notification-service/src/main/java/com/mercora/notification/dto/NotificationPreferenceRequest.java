package com.mercora.notification.dto;

public record NotificationPreferenceRequest(
        boolean emailEnabled,
        boolean smsEnabled,
        boolean inAppEnabled) {
}
