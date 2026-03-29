package com.mercora.notification.service;

import com.mercora.notification.dto.NotificationHistoryResponse;
import com.mercora.notification.dto.NotificationPreferenceRequest;
import com.mercora.notification.dto.NotificationPreferenceResponse;
import java.util.List;

public interface NotificationService {

    NotificationPreferenceResponse getPreferences(String userId);

    NotificationPreferenceResponse updatePreferences(String userId, NotificationPreferenceRequest request);

    List<NotificationHistoryResponse> getHistory(String userId);

    void notifyUser(String userId, String subject, String message, String eventType);
}
