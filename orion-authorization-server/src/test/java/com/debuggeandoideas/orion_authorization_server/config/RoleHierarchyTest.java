package com.debuggeandoideas.orion_authorization_server.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
public class RoleHierarchyTest {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_USER = "ROLE_USER";

    private final RoleHierarchy roleHierarchy = new RoleTokenConfig().roleHierarchy();


    @Test
    void adminManagerUser() {
        assertThat(reachableRole(ROLE_ADMIN)).contains(ROLE_MANAGER, ROLE_USER);
    }

    @Test
    void userDoesNotManagerAdmin() {
        assertThat(reachableRole(ROLE_USER)).doesNotContain(ROLE_MANAGER);
    }


    private Collection<String> reachableRole(String role) {
        Collection<? extends GrantedAuthority> reachable =
                roleHierarchy.getReachableGrantedAuthorities(AuthorityUtils.createAuthorityList(role));

        return AuthorityUtils.authorityListToSet(reachable);
    }
}
