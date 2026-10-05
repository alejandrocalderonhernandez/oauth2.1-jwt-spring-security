package com.debuggeandoideas.orion_resource_server.model;

import java.util.List;

public record Mission(
        String id,
        String codename,
        String destination,
        String status,
        String classification,
        String launchDate,
        String summary,
        List<CrewMember> crew,
        List<String> findings,
        String classifiedNote) {
}
