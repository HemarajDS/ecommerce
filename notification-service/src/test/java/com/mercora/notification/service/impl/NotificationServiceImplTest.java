package com.mercora.notification.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.notification.config.NotificationProperties;
import com.mercora.notification.dto.NotificationPreferenceRequest;
import com.mercora.notification.model.NotificationHistoryDocument;
import com.mercora.notification.model.NotificationPreferenceDocument;
import com.mercora.notification.repository.NotificationHistoryRepository;
import com.mercora.notification.repository.NotificationPreferenceRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private NotificationHistoryRepository historyRepository;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationProperties notificationProperties;

    @BeforeEach
    void setUp() {
        notificationProperties = new NotificationProperties();
        notificationProperties.setEmailEnabled(false);
        notificationProperties.setSmsEnabled(false);
        notificationProperties.setInAppEnabled(true);
        notificationService = new NotificationServiceImpl(
                preferenceRepository,
                historyRepository,
                notificationProperties,
                mailSender);
    }

    @Test
    void updatePreferencesShouldPersistUserSettings() {
        NotificationPreferenceDocument document = new NotificationPreferenceDocument();
        document.setUserId("user-1");
        document.setUpdatedAt(Instant.now());
        when(preferenceRepository.findById("user-1")).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreferenceDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = notificationService.updatePreferences("user-1", new NotificationPreferenceRequest(true, false, true));

        assertEquals("user-1", response.userId());
        assertEquals(true, response.emailEnabled());
    }

    @Test
    void notifyUserShouldWriteHistory() {
        when(preferenceRepository.findById("user-1")).thenReturn(Optional.empty());

        notificationService.notifyUser("user-1", "Order update", "Your order shipped", "order.shipped");

        verify(historyRepository, org.mockito.Mockito.atLeastOnce()).save(any(NotificationHistoryDocument.class));
    }
}
