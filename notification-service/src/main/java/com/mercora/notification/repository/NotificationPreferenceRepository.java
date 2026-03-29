package com.mercora.notification.repository;

import com.mercora.notification.model.NotificationPreferenceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreferenceDocument, String> {
}
