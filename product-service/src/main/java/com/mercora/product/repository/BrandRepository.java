package com.mercora.product.repository;

import com.mercora.product.model.BrandDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrandRepository extends MongoRepository<BrandDocument, String> {

    boolean existsBySlug(String slug);

    Optional<BrandDocument> findBySlug(String slug);
}
