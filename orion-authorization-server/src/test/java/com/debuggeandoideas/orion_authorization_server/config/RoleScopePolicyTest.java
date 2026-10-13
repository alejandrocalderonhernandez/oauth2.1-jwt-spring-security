package com.debuggeandoideas.orion_authorization_server.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleScopePolicyTest {

    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_USER = "ROLE_USER";

    private static final String SCOPE_MISSION_READ = "mission.read";
    private static final String SCOPE_MISSION_WRITE = "mission.write";

    private static final Set<String> REQUESTED_SCOPES = Set.of(SCOPE_MISSION_READ, SCOPE_MISSION_WRITE);

    private final RoleScopePolicy roleScopePolicy = new RoleScopePolicy();

    @Test
    void userReqReadAndWriteReceivedOnlyRead() {
        Set<String> grated = roleScopePolicy.filter(REQUESTED_SCOPES, List.of(ROLE_USER));

        assertThat(grated).containsExactly(SCOPE_MISSION_READ);
    }

    @Test
    void managerReqReadAndWriteReceivedOnlyBoth() {
        Set<String> grated = roleScopePolicy.filter(REQUESTED_SCOPES, List.of(ROLE_MANAGER));

        assertThat(grated).containsExactlyInAnyOrder(SCOPE_MISSION_READ, SCOPE_MISSION_WRITE);
    }

}
