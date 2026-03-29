package com.mercora.cms.dto;

import com.mercora.cms.model.PageStatus;
import java.time.Instant;
import java.util.List;

public record PageVersionResponse(
        int versionNumber,
        String pageTitle,
        SeoMetadataResponse seoMetadata,
        List<PageSectionResponse> sections,
        PageStatus status,
        Instant createdAt) {
}
