package com.mercora.order.repository;

import com.mercora.order.model.OrderDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderDocument, String> {

    Optional<OrderDocument> findByOrderNumber(String orderNumber);

    Optional<OrderDocument> findByPaymentSessionId(String paymentSessionId);

    List<OrderDocument> findByUserIdOrderByCreatedAtDesc(String userId);
}
