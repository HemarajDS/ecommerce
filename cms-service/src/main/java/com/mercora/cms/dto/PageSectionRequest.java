package com.mercora.cms.dto;

import com.mercora.cms.model.SectionType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record PageSectionRequest(
        String id,
        @NotNull SectionType type,
        String title,
        Map<String, Object> content) {
}
