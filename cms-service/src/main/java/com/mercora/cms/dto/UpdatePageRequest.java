package com.mercora.cms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdatePageRequest(
        @NotBlank String title,
        @Valid SeoMetadataRequest seoMetadata,
        @Valid @NotEmpty List<PageSectionRequest> sections,
        List<String> assetUrls) {
}
