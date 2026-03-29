package com.mercora.dealer.repository;

import com.mercora.dealer.model.LedgerEntryDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LedgerEntryRepository extends MongoRepository<LedgerEntryDocument, String> {

    List<LedgerEntryDocument> findByDealerIdOrderByCreatedAtDesc(String dealerId);
}
