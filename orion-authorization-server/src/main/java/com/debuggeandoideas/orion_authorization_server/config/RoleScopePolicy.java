package com.debuggeandoideas.orion_authorization_server.config;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class RoleScopePolicy {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_USER = "ROLE_USER";

    private static final String SCOPE_MISSION_READ = "mission.read";
    private static final String SCOPE_MISSION_WRITE = "mission.write";



    private static final Map<String, Set<String>> SCOPES_BY_ROLE = Map.of(
            ROLE_ADMIN, Set.of(SCOPE_MISSION_READ, SCOPE_MISSION_WRITE),
            ROLE_MANAGER, Set.of(SCOPE_MISSION_READ, SCOPE_MISSION_WRITE),
            ROLE_USER, Set.of(SCOPE_MISSION_READ));

    public Set<String> allowedScopes(Collection<String> roles) {
        Set<String> allowed = new LinkedHashSet<>();
        for (String role : roles) {
            allowed.addAll(SCOPES_BY_ROLE.getOrDefault(role, Set.of()));
        }
        return allowed;
    }

    public Set<String> filter(Set<String> requested, Collection<String> roles) {
        Set<String> allowed = allowedScopes(roles);
        Set<String> filtered = new LinkedHashSet<>();
        for (String scope : requested) {
            if (allowed.contains(scope)) {
                filtered.add(scope);
            }
        }
        return filtered;
    }
}
