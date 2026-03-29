package com.mercora.product.service.impl;

import com.mercora.product.dto.BrandRequest;
import com.mercora.product.dto.BrandResponse;
import com.mercora.product.dto.CategoryRequest;
import com.mercora.product.dto.CategoryResponse;
import com.mercora.product.dto.PageResponse;
import com.mercora.product.dto.ProductPriceResponse;
import com.mercora.product.dto.ProductRequest;
import com.mercora.product.dto.ProductResponse;
import com.mercora.product.dto.ProductSearchRequest;
import com.mercora.product.dto.ProductVariantRequest;
import com.mercora.product.dto.ProductVariantResponse;
import com.mercora.product.event.ProductEventPublisher;
import com.mercora.product.exception.BusinessRuleException;
import com.mercora.product.exception.ResourceNotFoundException;
import com.mercora.product.model.BrandDocument;
import com.mercora.product.model.CategoryDocument;
import com.mercora.product.model.ProductDocument;
import com.mercora.product.model.ProductVariant;
import com.mercora.product.repository.BrandRepository;
import com.mercora.product.repository.CategoryRepository;
import com.mercora.product.repository.ProductRepository;
import com.mercora.product.service.ProductCatalogService;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final ProductEventPublisher productEventPublisher;

    public ProductCatalogServiceImpl(
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            MongoTemplate mongoTemplate,
            ProductEventPublisher productEventPublisher) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.productEventPublisher = productEventPublisher;
    }

    @Override
    public BrandResponse createBrand(BrandRequest request) {
        String slug = slugify(request.name());
        if (brandRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("Brand already exists");
        }

        BrandDocument brand = new BrandDocument();
        brand.setName(request.name().trim());
        brand.setSlug(slug);
        brand.setDescription(request.description());
        brand.setCreatedAt(Instant.now());
        brand.setUpdatedAt(Instant.now());
        return toBrandResponse(brandRepository.save(brand));
    }

    @Override
    public List<BrandResponse> listBrands() {
        return brandRepository.findAll().stream().map(this::toBrandResponse).toList();
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = slugify(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("Category already exists");
        }
        if (request.parentId() != null && !request.parentId().isBlank()) {
            categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        CategoryDocument category = new CategoryDocument();
        category.setName(request.name().trim());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setParentId(blankToNull(request.parentId()));
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryResponse).toList();
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        validateBrandAndCategory(request.brandId(), request.categoryId());
        ensureUniqueSkus(request.variants());

        ProductDocument product = new ProductDocument();
        applyProductRequest(product, request);
        product.setSlug(createUniqueProductSlug(request.name()));
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());

        ProductDocument saved = productRepository.save(product);
        productEventPublisher.publishCreated(saved);
        return toProductResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        ProductDocument product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        validateBrandAndCategory(request.brandId(), request.categoryId());
        ensureUniqueSkus(request.variants());
        applyProductRequest(product, request);
        product.setUpdatedAt(Instant.now());

        ProductDocument saved = productRepository.save(product);
        productEventPublisher.publishUpdated(saved);
        return toProductResponse(saved);
    }

    @Override
    public ProductResponse getProduct(String productId) {
        return productRepository.findById(productId)
                .map(this::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Override
    public ProductPriceResponse getProductPrice(String productId, String sku) {
        ProductDocument product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductVariant variant = product.getVariants() == null ? null : product.getVariants().stream()
                .filter(entry -> entry.getSku().equalsIgnoreCase(sku))
                .findFirst()
                .orElse(null);

        if (variant == null) {
            throw new ResourceNotFoundException("Product variant not found");
        }

        return new ProductPriceResponse(product.getId(), variant.getSku(), variant.getName(), variant.getRetailPrice());
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(ProductSearchRequest request, int page, int size) {
        Query query = new Query().with(PageRequest.of(page, size));
        List<Criteria> criteriaList = new ArrayList<>();

        if (request.query() != null && !request.query().isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(request.query(), "i"),
                    Criteria.where("description").regex(request.query(), "i"),
                    Criteria.where("tags").regex(request.query(), "i")));
        }
        if (request.categoryId() != null && !request.categoryId().isBlank()) {
            criteriaList.add(Criteria.where("categoryId").is(request.categoryId()));
        }
        if (request.brandId() != null && !request.brandId().isBlank()) {
            criteriaList.add(Criteria.where("brandId").is(request.brandId()));
        }
        if (request.minPrice() != null || request.maxPrice() != null) {
            Criteria priceCriteria = Criteria.where("retailPrice");
            if (request.minPrice() != null) {
                priceCriteria = priceCriteria.gte(request.minPrice());
            }
            if (request.maxPrice() != null) {
                priceCriteria = priceCriteria.lte(request.maxPrice());
            }
            criteriaList.add(priceCriteria);
        }
        if (request.minRating() != null) {
            criteriaList.add(Criteria.where("rating").gte(request.minRating()));
        }
        if (request.status() != null) {
            criteriaList.add(Criteria.where("status").is(request.status()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(Criteria[]::new)));
        }

        List<ProductDocument> results = mongoTemplate.find(query, ProductDocument.class);
        Query countQuery = Query.of(query).limit(-1).skip(-1);
        long total = mongoTemplate.count(countQuery, ProductDocument.class);

        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PageResponse<>(
                results.stream().map(this::toProductResponse).toList(),
                page,
                size,
                total,
                totalPages);
    }

    private void validateBrandAndCategory(String brandId, String categoryId) {
        brandRepository.findById(brandId).orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private void ensureUniqueSkus(List<ProductVariantRequest> variants) {
        Set<String> skus = variants.stream().map(ProductVariantRequest::sku).map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
        if (skus.size() != variants.size()) {
            throw new BusinessRuleException("Variant SKUs must be unique");
        }
    }

    private void applyProductRequest(ProductDocument product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setBrandId(request.brandId());
        product.setCategoryId(request.categoryId());
        product.setTags(request.tags());
        product.setRetailPrice(request.retailPrice());
        product.setDealerPrice(request.dealerPrice());
        product.setRating(request.rating() == null ? 0.0 : request.rating());
        product.setStatus(request.status());
        product.setImageUrls(request.imageUrls());
        product.setAttributes(request.attributes());
        product.setVariants(request.variants().stream().map(this::toVariant).toList());
    }

    private ProductVariant toVariant(ProductVariantRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setSku(request.sku().trim());
        variant.setName(request.name().trim());
        variant.setColor(request.color());
        variant.setSize(request.size());
        variant.setRetailPrice(request.retailPrice());
        variant.setDealerPrice(request.dealerPrice());
        variant.setStockOnHand(request.stockOnHand());
        variant.setAttributes(request.attributes());
        return variant;
    }

    private String createUniqueProductSlug(String name) {
        String baseSlug = slugify(name);
        String candidate = baseSlug;
        int suffix = 1;
        while (productRepository.existsBySlug(candidate)) {
            suffix++;
            candidate = baseSlug + "-" + suffix;
        }
        return candidate;
    }

    private BrandResponse toBrandResponse(BrandDocument brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getSlug(), brand.getDescription());
    }

    private CategoryResponse toCategoryResponse(CategoryDocument category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParentId());
    }

    private ProductResponse toProductResponse(ProductDocument product) {
        return new ProductResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getDescription(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getTags(),
                product.getRetailPrice(),
                product.getDealerPrice(),
                product.getRating(),
                product.getStatus(),
                product.getImageUrls(),
                product.getAttributes(),
                product.getVariants() == null ? List.of() : product.getVariants().stream().map(this::toVariantResponse).toList(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getSku(),
                variant.getName(),
                variant.getColor(),
                variant.getSize(),
                variant.getRetailPrice(),
                variant.getDealerPrice(),
                variant.getStockOnHand(),
                variant.getAttributes());
    }

    private String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("").toLowerCase(Locale.ENGLISH);
        return slug.replaceAll("-{2,}", "-");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
