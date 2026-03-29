package com.mercora.inventory.repository;

import com.mercora.inventory.model.InventoryReservationDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryReservationRepository extends MongoRepository<InventoryReservationDocument, String> {

    Optional<InventoryReservationDocument> findByReservationCode(String reservationCode);

    List<InventoryReservationDocument> findByOrderId(String orderId);
}
