package com.mercora.cms.dto;

public record SeoMetadataResponse(
        String title,
        String description,
        String openGraphTitle,
        String openGraphDescription) {
}
