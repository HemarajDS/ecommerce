package com.mercora.inventory.repository;

import com.mercora.inventory.model.StockAdjustmentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockAdjustmentRepository extends MongoRepository<StockAdjustmentDocument, String> {
}
