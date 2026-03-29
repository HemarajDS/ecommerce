package com.mercora.notification.service.impl;

import com.mercora.notification.config.NotificationProperties;
import com.mercora.notification.dto.NotificationHistoryResponse;
import com.mercora.notification.dto.NotificationPreferenceRequest;
import com.mercora.notification.dto.NotificationPreferenceResponse;
import com.mercora.notification.model.NotificationChannel;
import com.mercora.notification.model.NotificationHistoryDocument;
import com.mercora.notification.model.NotificationPreferenceDocument;
import com.mercora.notification.model.NotificationStatus;
import com.mercora.notification.repository.NotificationHistoryRepository;
import com.mercora.notification.repository.NotificationPreferenceRepository;
import com.mercora.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationHistoryRepository historyRepository;
    private final NotificationProperties notificationProperties;
    private final JavaMailSender mailSender;

    public NotificationServiceImpl(
            NotificationPreferenceRepository preferenceRepository,
            NotificationHistoryRepository historyRepository,
            NotificationProperties notificationProperties,
            JavaMailSender mailSender) {
        this.preferenceRepository = preferenceRepository;
        this.historyRepository = historyRepository;
        this.notificationProperties = notificationProperties;
        this.mailSender = mailSender;
    }

    @Override
    public NotificationPreferenceResponse getPreferences(String userId) {
        return preferenceRepository.findById(userId)
                .map(this::toPreferenceResponse)
                .orElseGet(() -> defaultPreferences(userId));
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(String userId, NotificationPreferenceRequest request) {
        NotificationPreferenceDocument preference = preferenceRepository.findById(userId).orElseGet(NotificationPreferenceDocument::new);
        preference.setUserId(userId);
        preference.setEmailEnabled(request.emailEnabled());
        preference.setSmsEnabled(request.smsEnabled());
        preference.setInAppEnabled(request.inAppEnabled());
        preference.setUpdatedAt(Instant.now());
        return toPreferenceResponse(preferenceRepository.save(preference));
    }

    @Override
    public List<NotificationHistoryResponse> getHistory(String userId) {
        return historyRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    @Async("notificationExecutor")
    public void notifyUser(String userId, String subject, String message, String eventType) {
        NotificationPreferenceResponse preferences = getPreferences(userId);

        if (notificationProperties.isInAppEnabled() && preferences.inAppEnabled()) {
            saveHistory(userId, subject, message, NotificationChannel.IN_APP, NotificationStatus.SENT, eventType);
        }

        if (notificationProperties.isEmailEnabled() && preferences.emailEnabled()) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(userId + "@example.com");
                mail.setSubject(subject);
                mail.setText(message);
                mailSender.send(mail);
                saveHistory(userId, subject, message, NotificationChannel.EMAIL, NotificationStatus.SENT, eventType);
            } catch (Exception ex) {
                saveHistory(userId, subject, message, NotificationChannel.EMAIL, NotificationStatus.FAILED, eventType);
            }
        } else {
            saveHistory(userId, subject, message, NotificationChannel.EMAIL, NotificationStatus.SKIPPED, eventType);
        }

        if (notificationProperties.isSmsEnabled() && preferences.smsEnabled()) {
            saveHistory(userId, subject, message, NotificationChannel.SMS, NotificationStatus.SENT, eventType);
        } else {
            saveHistory(userId, subject, message, NotificationChannel.SMS, NotificationStatus.SKIPPED, eventType);
        }
    }

    private NotificationPreferenceResponse defaultPreferences(String userId) {
        return new NotificationPreferenceResponse(
                userId,
                notificationProperties.isEmailEnabled(),
                notificationProperties.isSmsEnabled(),
                notificationProperties.isInAppEnabled(),
                Instant.now());
    }

    private void saveHistory(String userId, String subject, String message, NotificationChannel channel, NotificationStatus status, String eventType) {
        NotificationHistoryDocument history = new NotificationHistoryDocument();
        history.setUserId(userId);
        history.setSubject(subject);
        history.setMessage(message);
        history.setChannel(channel);
        history.setStatus(status);
        history.setEventType(eventType);
        history.setCreatedAt(Instant.now());
        historyRepository.save(history);
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreferenceDocument document) {
        return new NotificationPreferenceResponse(
                document.getUserId(),
                document.isEmailEnabled(),
                document.isSmsEnabled(),
                document.isInAppEnabled(),
                document.getUpdatedAt());
    }

    private NotificationHistoryResponse toHistoryResponse(NotificationHistoryDocument document) {
        return new NotificationHistoryResponse(
                document.getId(),
                document.getUserId(),
                document.getSubject(),
                document.getMessage(),
                document.getChannel(),
                document.getStatus(),
                document.getEventType(),
                document.getCreatedAt());
    }
}
