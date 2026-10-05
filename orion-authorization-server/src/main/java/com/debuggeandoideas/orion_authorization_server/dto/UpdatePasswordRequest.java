package com.debuggeandoideas.orion_authorization_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank
        @Size(min = 8, max = 72)
        String newPassword) {
}
