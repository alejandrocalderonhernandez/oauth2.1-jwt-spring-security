package com.debuggeandoideas.orion_resource_server.dto;

import java.util.List;

public record MissionResponse(
        String id,
        String codename,
        String destination,
        String status,
        String classification,
        String launchDate,
        String summary,
        List<CrewMemberResponse> crew,
        List<String> findings,
        String classifiedNote) {
}
