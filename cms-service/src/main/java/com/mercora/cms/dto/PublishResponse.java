package com.mercora.cms.dto;

public record PublishResponse(
        String pageId,
        String status,
        String cacheInvalidationReference) {
}
