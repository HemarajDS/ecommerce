package com.mercora.cms.event;

import java.time.Instant;

public record CmsPublishEvent(
        String pageId,
        String slug,
        String distributionId,
        Instant occurredAt) {
}
