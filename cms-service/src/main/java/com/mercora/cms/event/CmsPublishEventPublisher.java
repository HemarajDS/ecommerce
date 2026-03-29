package com.mercora.cms.event;

import com.mercora.cms.config.CmsProperties;
import com.mercora.cms.model.PageDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CmsPublishEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CmsPublishEventPublisher.class);
    private final CmsProperties cmsProperties;

    public CmsPublishEventPublisher(CmsProperties cmsProperties) {
        this.cmsProperties = cmsProperties;
    }

    public String invalidateCache(PageDocument page) {
        String reference = cmsProperties.getCloudfrontDistributionId() + ":" + page.getSlug();
        log.info("cms_publish_invalidation distributionId={} pageSlug={}", cmsProperties.getCloudfrontDistributionId(), page.getSlug());
        return reference;
    }
}
