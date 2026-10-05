package com.debuggeandoideas.orion_authorization_server.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
        @NotBlank
        String role) {
}
