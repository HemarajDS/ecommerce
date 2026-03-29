package com.mercora.cms.model;

import java.time.Instant;
import java.util.List;

public class PageVersionSnapshot {

    private int versionNumber;
    private String pageTitle;
    private SeoMetadata seoMetadata;
    private List<PageSection> sections;
    private PageStatus status;
    private Instant createdAt;

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
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

    public PageStatus getStatus() {
        return status;
    }

    public void setStatus(PageStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
