package com.mercora.product.repository;

import com.mercora.product.model.CategoryDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<CategoryDocument, String> {

    boolean existsBySlug(String slug);

    List<CategoryDocument> findByParentId(String parentId);
}
