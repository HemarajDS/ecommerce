package com.mercora.product.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.product.dto.ProductRequest;
import com.mercora.product.dto.ProductVariantRequest;
import com.mercora.product.event.ProductEventPublisher;
import com.mercora.product.exception.BusinessRuleException;
import com.mercora.product.model.BrandDocument;
import com.mercora.product.model.CategoryDocument;
import com.mercora.product.model.ProductDocument;
import com.mercora.product.model.ProductStatus;
import com.mercora.product.repository.BrandRepository;
import com.mercora.product.repository.CategoryRepository;
import com.mercora.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceImplTest {

    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private ProductCatalogServiceImpl productCatalogService;

    @Test
    void createProductShouldRejectDuplicateVariantSkus() {
        ProductRequest request = new ProductRequest(
                "Trail Shoes",
                "Rugged outdoor shoes",
                "brand-1",
                "category-1",
                List.of("outdoor"),
                BigDecimal.valueOf(1999),
                BigDecimal.valueOf(1499),
                4.7,
                ProductStatus.PUBLISHED,
                List.of("https://assets.mercora.local/trail-shoes.png"),
                Map.of("material", "mesh"),
                List.of(
                        new ProductVariantRequest("SKU-1", "Trail Shoes 9", "Black", "9", BigDecimal.valueOf(1999), BigDecimal.valueOf(1499), 10, Map.of()),
                        new ProductVariantRequest("SKU-1", "Trail Shoes 10", "Black", "10", BigDecimal.valueOf(1999), BigDecimal.valueOf(1499), 10, Map.of())));

        when(brandRepository.findById("brand-1")).thenReturn(Optional.of(new BrandDocument()));
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(new CategoryDocument()));

        assertThrows(BusinessRuleException.class, () -> productCatalogService.createProduct(request));
    }

    @Test
    void createProductShouldPersistAndPublishEvent() {
        BrandDocument brand = new BrandDocument();
        brand.setId("brand-1");
        CategoryDocument category = new CategoryDocument();
        category.setId("category-1");

        ProductRequest request = new ProductRequest(
                "Trail Shoes",
                "Rugged outdoor shoes",
                "brand-1",
                "category-1",
                List.of("outdoor"),
                BigDecimal.valueOf(1999),
                BigDecimal.valueOf(1499),
                4.7,
                ProductStatus.PUBLISHED,
                List.of("https://assets.mercora.local/trail-shoes.png"),
                Map.of("material", "mesh"),
                List.of(new ProductVariantRequest("SKU-1", "Trail Shoes 9", "Black", "9", BigDecimal.valueOf(1999), BigDecimal.valueOf(1499), 10, Map.of())));

        when(brandRepository.findById("brand-1")).thenReturn(Optional.of(brand));
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));
        when(productRepository.existsBySlug("trail-shoes")).thenReturn(false);
        when(productRepository.save(any(ProductDocument.class))).thenAnswer(invocation -> {
            ProductDocument document = invocation.getArgument(0);
            document.setId("product-1");
            document.setCreatedAt(Instant.now());
            document.setUpdatedAt(Instant.now());
            return document;
        });

        var response = productCatalogService.createProduct(request);

        assertEquals("product-1", response.id());
        assertEquals("trail-shoes", response.slug());
        verify(productEventPublisher).publishCreated(any(ProductDocument.class));
    }
}
