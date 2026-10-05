package com.debuggeandoideas.orion_resource_server.controller;

import com.debuggeandoideas.orion_resource_server.dto.NewsResponse;
import com.debuggeandoideas.orion_resource_server.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsResponse> findAll() {
        return newsService.findAll();
    }
}
