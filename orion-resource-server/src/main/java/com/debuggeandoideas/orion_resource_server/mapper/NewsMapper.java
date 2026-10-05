package com.debuggeandoideas.orion_resource_server.mapper;

import com.debuggeandoideas.orion_resource_server.dto.NewsResponse;
import com.debuggeandoideas.orion_resource_server.model.News;

public final class NewsMapper {

    private NewsMapper() {
    }

    public static NewsResponse toResponse(News news) {
        return new NewsResponse(news.id(), news.date(), news.tag(), news.headline(), news.summary());
    }
}
