package com.mercora.notification.controller;

import com.mercora.notification.dto.NotificationHistoryResponse;
import com.mercora.notification.dto.NotificationPreferenceRequest;
import com.mercora.notification.dto.NotificationPreferenceResponse;
import com.mercora.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification preferences and history APIs")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public NotificationPreferenceResponse getPreferences(@RequestHeader("X-User-Id") String userId) {
        return notificationService.getPreferences(userId);
    }

    @PatchMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public NotificationPreferenceResponse updatePreferences(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody NotificationPreferenceRequest request) {
        return notificationService.updatePreferences(userId, request);
    }

    @GetMapping("/history")
    @Operation(summary = "Get notification history")
    public List<NotificationHistoryResponse> getHistory(@RequestHeader("X-User-Id") String userId) {
        return notificationService.getHistory(userId);
    }
}
