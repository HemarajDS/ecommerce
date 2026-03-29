package com.mercora.cms.dto;

import com.mercora.cms.model.SectionType;
import java.util.Map;

public record PageSectionResponse(
        String id,
        SectionType type,
        String title,
        Map<String, Object> content) {
}
