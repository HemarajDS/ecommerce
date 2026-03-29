package com.mercora.inventory.repository;

import com.mercora.inventory.model.InventoryItemDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryItemRepository extends MongoRepository<InventoryItemDocument, String> {

    Optional<InventoryItemDocument> findByProductIdAndWarehouseCode(String productId, String warehouseCode);

    List<InventoryItemDocument> findByProductId(String productId);

    List<InventoryItemDocument> findBySku(String sku);
}
