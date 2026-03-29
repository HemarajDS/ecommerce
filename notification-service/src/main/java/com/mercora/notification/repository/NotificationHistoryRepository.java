package com.mercora.notification.repository;

import com.mercora.notification.model.NotificationHistoryDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationHistoryRepository extends MongoRepository<NotificationHistoryDocument, String> {

    List<NotificationHistoryDocument> findTop50ByUserIdOrderByCreatedAtDesc(String userId);
}
