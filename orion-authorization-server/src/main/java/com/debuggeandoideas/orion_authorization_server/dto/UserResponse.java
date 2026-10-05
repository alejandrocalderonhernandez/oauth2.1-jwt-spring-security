package com.debuggeandoideas.orion_authorization_server.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        String role,
        Instant createdAt) {
}
