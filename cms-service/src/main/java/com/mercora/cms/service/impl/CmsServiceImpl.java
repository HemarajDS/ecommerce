package com.mercora.cms.service.impl;

import com.mercora.cms.dto.CreatePageRequest;
import com.mercora.cms.dto.PageResponse;
import com.mercora.cms.dto.PageSectionRequest;
import com.mercora.cms.dto.PageSectionResponse;
import com.mercora.cms.dto.PageVersionResponse;
import com.mercora.cms.dto.PublishResponse;
import com.mercora.cms.dto.SeoMetadataRequest;
import com.mercora.cms.dto.SeoMetadataResponse;
import com.mercora.cms.dto.UpdatePageRequest;
import com.mercora.cms.event.CmsPublishEventPublisher;
import com.mercora.cms.exception.BusinessRuleException;
import com.mercora.cms.exception.ResourceNotFoundException;
import com.mercora.cms.model.PageDocument;
import com.mercora.cms.model.PageSection;
import com.mercora.cms.model.PageStatus;
import com.mercora.cms.model.PageVersionSnapshot;
import com.mercora.cms.model.SeoMetadata;
import com.mercora.cms.repository.PageRepository;
import com.mercora.cms.service.CmsService;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CmsServiceImpl implements CmsService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final PageRepository pageRepository;
    private final CmsPublishEventPublisher publishEventPublisher;

    public CmsServiceImpl(PageRepository pageRepository, CmsPublishEventPublisher publishEventPublisher) {
        this.pageRepository = pageRepository;
        this.publishEventPublisher = publishEventPublisher;
    }

    @Override
    public PageResponse createPage(CreatePageRequest request) {
        String slug = createUniqueSlug(request.title());
        PageDocument page = new PageDocument();
        page.setSlug(slug);
        page.setTitle(request.title());
        page.setStatus(PageStatus.DRAFT);
        page.setSeoMetadata(toSeo(request.seoMetadata()));
        page.setSections(request.sections().stream().map(this::toSection).toList());
        page.setAssetUrls(request.assetUrls());
        page.setVersions(new ArrayList<>());
        page.setCreatedAt(Instant.now());
        page.setUpdatedAt(Instant.now());
        snapshot(page);
        return toResponse(pageRepository.save(page));
    }

    @Override
    public PageResponse updatePage(String pageId, UpdatePageRequest request) {
        PageDocument page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        if (page.getStatus() == PageStatus.PUBLISHED) {
            throw new BusinessRuleException("Published pages should be moved to preview before editing");
        }
        page.setTitle(request.title());
        page.setSeoMetadata(toSeo(request.seoMetadata()));
        page.setSections(request.sections().stream().map(this::toSection).toList());
        page.setAssetUrls(request.assetUrls());
        page.setUpdatedAt(Instant.now());
        snapshot(page);
        return toResponse(pageRepository.save(page));
    }

    @Override
    public PageResponse getPage(String pageId) {
        return pageRepository.findById(pageId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
    }

    @Override
    public List<PageResponse> listPages() {
        return pageRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse previewPage(String pageId) {
        PageDocument page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        page.setStatus(PageStatus.PREVIEW);
        page.setUpdatedAt(Instant.now());
        snapshot(page);
        return toResponse(pageRepository.save(page));
    }

    @Override
    public PublishResponse publishPage(String pageId) {
        PageDocument page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        page.setStatus(PageStatus.PUBLISHED);
        page.setUpdatedAt(Instant.now());
        snapshot(page);
        PageDocument saved = pageRepository.save(page);
        String invalidationReference = publishEventPublisher.invalidateCache(saved);
        return new PublishResponse(saved.getId(), saved.getStatus().name(), invalidationReference);
    }

    @Override
    public PageResponse rollbackPage(String pageId, int versionNumber) {
        PageDocument page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        PageVersionSnapshot snapshot = page.getVersions().stream()
                .filter(version -> version.getVersionNumber() == versionNumber)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Page version not found"));

        page.setTitle(snapshot.getPageTitle());
        page.setSeoMetadata(snapshot.getSeoMetadata());
        page.setSections(snapshot.getSections());
        page.setStatus(PageStatus.DRAFT);
        page.setUpdatedAt(Instant.now());
        snapshot(page);
        return toResponse(pageRepository.save(page));
    }

    private void snapshot(PageDocument page) {
        if (page.getVersions() == null) {
            page.setVersions(new ArrayList<>());
        }
        PageVersionSnapshot version = new PageVersionSnapshot();
        version.setVersionNumber(page.getVersions().size() + 1);
        version.setPageTitle(page.getTitle());
        version.setSeoMetadata(page.getSeoMetadata());
        version.setSections(page.getSections());
        version.setStatus(page.getStatus());
        version.setCreatedAt(Instant.now());
        page.getVersions().add(version);
    }

    private SeoMetadata toSeo(SeoMetadataRequest request) {
        if (request == null) {
            return null;
        }
        SeoMetadata seo = new SeoMetadata();
        seo.setTitle(request.title());
        seo.setDescription(request.description());
        seo.setOpenGraphTitle(request.openGraphTitle());
        seo.setOpenGraphDescription(request.openGraphDescription());
        return seo;
    }

    private PageSection toSection(PageSectionRequest request) {
        PageSection section = new PageSection();
        section.setId(request.id() == null || request.id().isBlank() ? java.util.UUID.randomUUID().toString() : request.id());
        section.setType(request.type());
        section.setTitle(request.title());
        section.setContent(request.content());
        return section;
    }

    private PageResponse toResponse(PageDocument page) {
        return new PageResponse(
                page.getId(),
                page.getSlug(),
                page.getTitle(),
                page.getStatus(),
                page.getSeoMetadata() == null ? null : new SeoMetadataResponse(
                        page.getSeoMetadata().getTitle(),
                        page.getSeoMetadata().getDescription(),
                        page.getSeoMetadata().getOpenGraphTitle(),
                        page.getSeoMetadata().getOpenGraphDescription()),
                page.getSections() == null ? List.of() : page.getSections().stream()
                        .map(section -> new PageSectionResponse(
                                section.getId(),
                                section.getType(),
                                section.getTitle(),
                                section.getContent()))
                        .toList(),
                page.getAssetUrls(),
                page.getVersions() == null ? List.of() : page.getVersions().stream()
                        .map(version -> new PageVersionResponse(
                                version.getVersionNumber(),
                                version.getPageTitle(),
                                version.getSeoMetadata() == null ? null : new SeoMetadataResponse(
                                        version.getSeoMetadata().getTitle(),
                                        version.getSeoMetadata().getDescription(),
                                        version.getSeoMetadata().getOpenGraphTitle(),
                                        version.getSeoMetadata().getOpenGraphDescription()),
                                version.getSections() == null ? List.of() : version.getSections().stream()
                                        .map(section -> new PageSectionResponse(
                                                section.getId(),
                                                section.getType(),
                                                section.getTitle(),
                                                section.getContent()))
                                        .toList(),
                                version.getStatus(),
                                version.getCreatedAt()))
                        .toList(),
                page.getCreatedAt(),
                page.getUpdatedAt());
    }

    private String createUniqueSlug(String title) {
        String baseSlug = slugify(title);
        String candidate = baseSlug;
        int suffix = 1;
        while (pageRepository.existsBySlug(candidate)) {
            suffix++;
            candidate = baseSlug + "-" + suffix;
        }
        return candidate;
    }

    private String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        return NON_LATIN.matcher(normalized).replaceAll("").toLowerCase(Locale.ENGLISH).replaceAll("-{2,}", "-");
    }
}
