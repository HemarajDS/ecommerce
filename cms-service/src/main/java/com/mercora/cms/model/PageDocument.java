package com.mercora.cms.model;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cms_pages")
public class PageDocument {

    @Id
    private String id;
    @Indexed(unique = true)
    private String slug;
    private String title;
    private PageStatus status;
    private SeoMetadata seoMetadata;
    private List<PageSection> sections;
    private List<String> assetUrls;
    private List<PageVersionSnapshot> versions;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PageStatus getStatus() {
        return status;
    }

    public void setStatus(PageStatus status) {
        this.status = status;
    }

    public SeoMetadata getSeoMetadata() {
        return seoMetadata;
    }

    public void setSeoMetadata(SeoMetadata seoMetadata) {
        this.seoMetadata = seoMetadata;
    }

    public List<PageSection> getSections() {
        return sections;
    }

    public void setSections(List<PageSection> sections) {
        this.sections = sections;
    }

    public List<String> getAssetUrls() {
        return assetUrls;
    }

    public void setAssetUrls(List<String> assetUrls) {
        this.assetUrls = assetUrls;
    }

    public List<PageVersionSnapshot> getVersions() {
        return versions;
    }

    public void setVersions(List<PageVersionSnapshot> versions) {
        this.versions = versions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
