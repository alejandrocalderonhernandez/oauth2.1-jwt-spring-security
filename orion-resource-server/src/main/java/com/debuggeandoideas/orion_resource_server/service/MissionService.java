package com.debuggeandoideas.orion_resource_server.service;

import com.debuggeandoideas.orion_resource_server.dto.CreatedMissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.MissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.NewMissionRequest;
import com.debuggeandoideas.orion_resource_server.mapper.MissionMapper;
import com.debuggeandoideas.orion_resource_server.store.MissionControlStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissionService {

    static final CreatedMissionResponse CREATED_MISSION = new CreatedMissionResponse(
            "M-005",
            "PENDIENTE_DE_REVISION",
            "Misión recibida por el centro de control. Un comité muy serio la revisará (mañana, quizá).");

    private final MissionControlStore missionControlStore;

    public MissionService(MissionControlStore missionControlStore) {
        this.missionControlStore = missionControlStore;
    }

    public List<MissionResponse> findAll() {
        return missionControlStore.findAllMissions().stream()
                .map(MissionMapper::toResponse)
                .toList();
    }

    public CreatedMissionResponse create(NewMissionRequest request) {
        return CREATED_MISSION;
    }
}
