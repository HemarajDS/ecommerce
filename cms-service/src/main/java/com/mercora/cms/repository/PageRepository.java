package com.mercora.cms.repository;

import com.mercora.cms.model.PageDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PageRepository extends MongoRepository<PageDocument, String> {

    boolean existsBySlug(String slug);

    Optional<PageDocument> findBySlug(String slug);
}
