package com.mercora.cms.controller;

import com.mercora.cms.dto.CreatePageRequest;
import com.mercora.cms.dto.PageResponse;
import com.mercora.cms.dto.PublishResponse;
import com.mercora.cms.dto.UpdatePageRequest;
import com.mercora.cms.service.CmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cms/pages")
@Tag(name = "CMS", description = "CMS page management APIs")
public class CmsController {

    private final CmsService cmsService;

    public CmsController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a CMS page")
    public PageResponse createPage(@Valid @RequestBody CreatePageRequest request) {
        return cmsService.createPage(request);
    }

    @PutMapping("/{pageId}")
    @Operation(summary = "Update a CMS page")
    public PageResponse updatePage(@PathVariable String pageId, @Valid @RequestBody UpdatePageRequest request) {
        return cmsService.updatePage(pageId, request);
    }

    @GetMapping("/{pageId}")
    @Operation(summary = "Get a CMS page")
    public PageResponse getPage(@PathVariable String pageId) {
        return cmsService.getPage(pageId);
    }

    @GetMapping
    @Operation(summary = "List CMS pages")
    public List<PageResponse> listPages() {
        return cmsService.listPages();
    }

    @PostMapping("/{pageId}/preview")
    @Operation(summary = "Move a page to preview")
    public PageResponse preview(@PathVariable String pageId) {
        return cmsService.previewPage(pageId);
    }

    @PostMapping("/{pageId}/publish")
    @Operation(summary = "Publish a page")
    public PublishResponse publish(@PathVariable String pageId) {
        return cmsService.publishPage(pageId);
    }

    @PostMapping("/{pageId}/rollback")
    @Operation(summary = "Rollback a page to a previous version")
    public PageResponse rollback(@PathVariable String pageId, @RequestParam int version) {
        return cmsService.rollbackPage(pageId, version);
    }
}
