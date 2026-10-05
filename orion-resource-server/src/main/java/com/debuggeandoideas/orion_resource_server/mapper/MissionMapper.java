package com.debuggeandoideas.orion_resource_server.mapper;

import com.debuggeandoideas.orion_resource_server.dto.CrewMemberResponse;
import com.debuggeandoideas.orion_resource_server.dto.MissionResponse;
import com.debuggeandoideas.orion_resource_server.model.CrewMember;
import com.debuggeandoideas.orion_resource_server.model.Mission;

import java.util.List;

public final class MissionMapper {

    private MissionMapper() {
    }

    public static MissionResponse toResponse(Mission mission) {
        return new MissionResponse(
                mission.id(),
                mission.codename(),
                mission.destination(),
                mission.status(),
                mission.classification(),
                mission.launchDate(),
                mission.summary(),
                toCrewResponses(mission.crew()),
                mission.findings() == null ? List.of() : List.copyOf(mission.findings()),
                mission.classifiedNote());
    }

    private static List<CrewMemberResponse> toCrewResponses(List<CrewMember> crew) {
        if (crew == null) {
            return List.of();
        }
        return crew.stream()
                .map(member -> new CrewMemberResponse(member.name(), member.specialty()))
                .toList();
    }
}
