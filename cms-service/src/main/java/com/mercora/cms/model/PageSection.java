package com.mercora.cms.model;

import java.util.Map;

public class PageSection {

    private String id;
    private SectionType type;
    private String title;
    private Map<String, Object> content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SectionType getType() {
        return type;
    }

    public void setType(SectionType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }
}
