package com.mercora.dealer.repository;

import com.mercora.dealer.model.DealerDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DealerRepository extends MongoRepository<DealerDocument, String> {

    Optional<DealerDocument> findByUserId(String userId);

    boolean existsByDealerCode(String dealerCode);
}
