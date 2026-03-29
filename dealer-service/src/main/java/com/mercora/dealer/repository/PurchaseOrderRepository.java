package com.mercora.dealer.repository;

import com.mercora.dealer.model.PurchaseOrderDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PurchaseOrderRepository extends MongoRepository<PurchaseOrderDocument, String> {

    List<PurchaseOrderDocument> findByDealerIdOrderByCreatedAtDesc(String dealerId);
}
