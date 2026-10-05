package com.debuggeandoideas.orion_resource_server.controller;

import com.debuggeandoideas.orion_resource_server.dto.CreatedMissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.MissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.NewMissionRequest;
import com.debuggeandoideas.orion_resource_server.service.MissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public List<MissionResponse> findAll() {
        return missionService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedMissionResponse create(@RequestBody(required = false) NewMissionRequest request) {
        return missionService.create(request);
    }
}
