package com.debuggeandoideas.orion_authorization_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.List;

@Configuration
public class RoleTokenConfig {

    private static final String ROLES_HIERARCHY = """
            ROLE_ADMIN > ROLE_MANAGER
            ROLE_MANAGER > ROLE_USER
            """;

    @Bean
    RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(ROLES_HIERARCHY);
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer(RoleHierarchy roleHierarchy, RoleScopePolicy roleScopePolicy) {
        return context -> {

            if (!OAuth2TokenType.ACCESS_TOKEN.getValue().equals(context.getTokenType().getValue())) {
                return;
            }

            List<String> roles = roleHierarchy
                    .getReachableGrantedAuthorities(context.getPrincipal().getAuthorities())
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth != null && auth.startsWith("ROLE_"))
                    .distinct()
                    .sorted()
                    .toList();

            context.getClaims().claim("roles", roles);
            context.getClaims().claim("scope", roleScopePolicy.filter(context.getAuthorizedScopes(), roles));
        };
    }
}

