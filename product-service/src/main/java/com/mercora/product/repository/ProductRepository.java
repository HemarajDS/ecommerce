package com.mercora.product.repository;

import com.mercora.product.model.ProductDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<ProductDocument, String> {

    boolean existsBySlug(String slug);

    Optional<ProductDocument> findBySlug(String slug);
}
