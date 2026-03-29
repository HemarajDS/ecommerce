package com.mercora.product.service;

import com.mercora.product.dto.BrandRequest;
import com.mercora.product.dto.BrandResponse;
import com.mercora.product.dto.CategoryRequest;
import com.mercora.product.dto.CategoryResponse;
import com.mercora.product.dto.PageResponse;
import com.mercora.product.dto.ProductPriceResponse;
import com.mercora.product.dto.ProductRequest;
import com.mercora.product.dto.ProductResponse;
import com.mercora.product.dto.ProductSearchRequest;
import java.util.List;

public interface ProductCatalogService {

    BrandResponse createBrand(BrandRequest request);

    List<BrandResponse> listBrands();

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> listCategories();

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(String productId, ProductRequest request);

    ProductResponse getProduct(String productId);

    ProductPriceResponse getProductPrice(String productId, String sku);

    PageResponse<ProductResponse> searchProducts(ProductSearchRequest request, int page, int size);
}
