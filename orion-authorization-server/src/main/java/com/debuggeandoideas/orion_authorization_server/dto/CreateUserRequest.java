package com.debuggeandoideas.orion_authorization_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "must contain only letters, digits, '.', '_' or '-'")
        String username,

        @NotBlank
        @Size(min = 8, max = 72)
        /*@Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$",
                message = "must have 8 to 72 characters, with at least one uppercase letter, one digit and one special character"
        )*/

        String password,

        String role) {
}
