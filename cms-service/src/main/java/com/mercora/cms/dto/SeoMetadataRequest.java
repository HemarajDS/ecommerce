package com.mercora.cms.dto;

public record SeoMetadataRequest(
        String title,
        String description,
        String openGraphTitle,
        String openGraphDescription) {
}
