package com.mercora.product.controller;

import com.mercora.product.dto.BrandRequest;
import com.mercora.product.dto.BrandResponse;
import com.mercora.product.dto.CategoryRequest;
import com.mercora.product.dto.CategoryResponse;
import com.mercora.product.dto.PageResponse;
import com.mercora.product.dto.ProductPriceResponse;
import com.mercora.product.dto.ProductRequest;
import com.mercora.product.dto.ProductResponse;
import com.mercora.product.dto.ProductSearchRequest;
import com.mercora.product.service.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product Catalog", description = "Catalog management APIs")
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    public ProductCatalogController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @PostMapping("/catalog/brands")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a brand")
    public BrandResponse createBrand(@Valid @RequestBody BrandRequest request) {
        return productCatalogService.createBrand(request);
    }

    @GetMapping("/catalog/brands")
    @Operation(summary = "List all brands")
    public List<BrandResponse> listBrands() {
        return productCatalogService.listBrands();
    }

    @PostMapping("/catalog/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a category")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return productCatalogService.createCategory(request);
    }

    @GetMapping("/catalog/categories")
    @Operation(summary = "List all categories")
    public List<CategoryResponse> listCategories() {
        return productCatalogService.listCategories();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productCatalogService.createProduct(request);
    }

    @PutMapping("/products/{productId}")
    @Operation(summary = "Update a product")
    public ProductResponse updateProduct(@PathVariable String productId, @Valid @RequestBody ProductRequest request) {
        return productCatalogService.updateProduct(productId, request);
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get a product by ID")
    public ProductResponse getProduct(@PathVariable String productId) {
        return productCatalogService.getProduct(productId);
    }

    @GetMapping("/products/{productId}/price")
    @Operation(summary = "Get live product pricing for a specific SKU")
    public ProductPriceResponse getProductPrice(@PathVariable String productId, @RequestParam String sku) {
        return productCatalogService.getProductPrice(productId, sku);
    }

    @GetMapping("/products")
    @Operation(summary = "Search products with filters and pagination")
    public PageResponse<ProductResponse> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) com.mercora.product.model.ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ProductSearchRequest request = new ProductSearchRequest(query, categoryId, brandId, minPrice, maxPrice, minRating, status);
        return productCatalogService.searchProducts(request, page, size);
    }
}
