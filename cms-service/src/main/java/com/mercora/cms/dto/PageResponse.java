package com.mercora.cms.dto;

import com.mercora.cms.model.PageStatus;
import java.time.Instant;
import java.util.List;

public record PageResponse(
        String id,
        String slug,
        String title,
        PageStatus status,
        SeoMetadataResponse seoMetadata,
        List<PageSectionResponse> sections,
        List<String> assetUrls,
        List<PageVersionResponse> versions,
        Instant createdAt,
        Instant updatedAt) {
}
