package com.debuggeandoideas.orion_authorization_server.util;

import com.debuggeandoideas.orion_authorization_server.dto.UserResponse;
import com.debuggeandoideas.orion_authorization_server.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.getRole().getName(),
                user.getCreatedAt());
    }
}
