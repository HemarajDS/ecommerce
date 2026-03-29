package com.mercora.payment.repository;

import com.mercora.payment.model.PaymentDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<PaymentDocument, String> {

    Optional<PaymentDocument> findByPaymentSessionId(String paymentSessionId);

    Optional<PaymentDocument> findByIdempotencyKey(String idempotencyKey);
}
