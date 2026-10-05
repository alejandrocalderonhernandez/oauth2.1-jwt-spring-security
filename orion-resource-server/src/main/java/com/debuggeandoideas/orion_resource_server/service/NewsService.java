package com.debuggeandoideas.orion_resource_server.service;

import com.debuggeandoideas.orion_resource_server.dto.NewsResponse;
import com.debuggeandoideas.orion_resource_server.mapper.NewsMapper;
import com.debuggeandoideas.orion_resource_server.store.MissionControlStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsService {

    private final MissionControlStore missionControlStore;

    public NewsService(MissionControlStore missionControlStore) {
        this.missionControlStore = missionControlStore;
    }

    public List<NewsResponse> findAll() {
        return missionControlStore.findAllNews().stream()
                .map(NewsMapper::toResponse)
                .toList();
    }
}
