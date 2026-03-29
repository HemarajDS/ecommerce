package com.mercora.cart.service;

import com.mercora.cart.config.FeignConfig;
import com.mercora.cart.dto.ProductPriceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductCatalogClient {

    @GetMapping("/api/v1/products/{productId}/price")
    @CircuitBreaker(name = "productService")
    ProductPriceResponse getPrice(@PathVariable("productId") String productId, @RequestParam("sku") String sku);
}
