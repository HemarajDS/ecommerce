package com.mercora.cms.service;

import com.mercora.cms.dto.CreatePageRequest;
import com.mercora.cms.dto.PageResponse;
import com.mercora.cms.dto.PublishResponse;
import com.mercora.cms.dto.UpdatePageRequest;
import java.util.List;

public interface CmsService {

    PageResponse createPage(CreatePageRequest request);

    PageResponse updatePage(String pageId, UpdatePageRequest request);

    PageResponse getPage(String pageId);

    List<PageResponse> listPages();

    PageResponse previewPage(String pageId);

    PublishResponse publishPage(String pageId);

    PageResponse rollbackPage(String pageId, int versionNumber);
}
