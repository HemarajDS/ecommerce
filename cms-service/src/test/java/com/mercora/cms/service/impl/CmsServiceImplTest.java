package com.mercora.cms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.cms.dto.CreatePageRequest;
import com.mercora.cms.dto.PageSectionRequest;
import com.mercora.cms.dto.SeoMetadataRequest;
import com.mercora.cms.dto.UpdatePageRequest;
import com.mercora.cms.event.CmsPublishEventPublisher;
import com.mercora.cms.exception.BusinessRuleException;
import com.mercora.cms.model.PageDocument;
import com.mercora.cms.model.PageStatus;
import com.mercora.cms.model.SectionType;
import com.mercora.cms.repository.PageRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CmsServiceImplTest {

    @Mock
    private PageRepository pageRepository;
    @Mock
    private CmsPublishEventPublisher publishEventPublisher;

    @InjectMocks
    private CmsServiceImpl cmsService;

    @Test
    void createPageShouldPersistDraftPage() {
        when(pageRepository.existsBySlug("home-page")).thenReturn(false);
        when(pageRepository.save(any(PageDocument.class))).thenAnswer(invocation -> {
            PageDocument page = invocation.getArgument(0);
            page.setId("page-1");
            return page;
        });

        var response = cmsService.createPage(new CreatePageRequest(
                "Home Page",
                new SeoMetadataRequest("Home", "Welcome", "Home OG", "Home OG Desc"),
                List.of(new PageSectionRequest(null, SectionType.HERO, "Hero", Map.of("headline", "Welcome to Mercora"))),
                List.of("https://assets.mercora.local/cms/home.png")));

        assertEquals(PageStatus.DRAFT, response.status());
        assertEquals("home-page", response.slug());
    }

    @Test
    void updatePublishedPageShouldFail() {
        PageDocument page = new PageDocument();
        page.setId("page-1");
        page.setStatus(PageStatus.PUBLISHED);
        page.setVersions(new ArrayList<>());
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(page));

        assertThrows(BusinessRuleException.class, () -> cmsService.updatePage(
                "page-1",
                new UpdatePageRequest(
                        "Updated",
                        new SeoMetadataRequest("Title", "Desc", "OG", "OG Desc"),
                        List.of(new PageSectionRequest(null, SectionType.FEATURES, "Features", Map.of())),
                        List.of())));
    }
}
