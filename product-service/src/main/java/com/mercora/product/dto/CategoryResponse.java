package com.mercora.product.dto;

public record CategoryResponse(
        String id,
        String name,
        String slug,
        String description,
        String parentId) {
}
