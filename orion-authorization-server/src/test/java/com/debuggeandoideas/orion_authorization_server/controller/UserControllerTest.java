package com.debuggeandoideas.orion_authorization_server.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUserWithDefaultRole() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "alice", "password": "alice1234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/users/" + idOf("alice"))))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.accountNonExpired").value(true))
                .andExpect(jsonPath("$.accountNonLocked").value(true))
                .andExpect(jsonPath("$.credentialsNonExpired").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUserWithExplicitRole() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "bob", "password": "bob12345", "role": "ROLE_MANAGER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_MANAGER"));
    }

    @Test
    void createUserWithUnknownRoleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "carol", "password": "carol1234", "role": "ROLE_GHOST"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Role not found"));
    }

    @Test
    void createUserWithDuplicateUsernameReturnsConflict() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "another1234"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Username 'admin' already exists"));
    }

    @Test
    void createUserWithInvalidBodyReturnsBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "a!", "password": "short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void findById() throws Exception {
        long id = idOf("manager");
        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value("manager"))
                .andExpect(jsonPath("$.role").value("ROLE_MANAGER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void findByUsername() throws Exception {
        mockMvc.perform(get("/users/by-username/{username}", "blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("blocked"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void findByUnknownUsernameReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/by-username/{username}", "nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItems("admin", "manager", "user", "blocked")))
                .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @Test
    void updatePassword() throws Exception {
        mockMvc.perform(patch("/users/{id}/password", idOf("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword": "brandNew1234"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePasswordWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/{id}/password", idOf("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void changeRoleToExistingRole() throws Exception {
        long id = idOf("user");
        mockMvc.perform(patch("/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "ROLE_MANAGER"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_MANAGER"));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(jsonPath("$.role").value("ROLE_MANAGER"));
    }

    @Test
    void changeRoleToNonExistentRoleReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/{id}/role", idOf("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "ROLE_GHOST"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Role 'ROLE_GHOST' does not exist"));
    }

    @ParameterizedTest
    @CsvSource({
            "disable, enable, enabled",
            "lock, unlock, accountNonLocked",
            "expire, renew, accountNonExpired",
            "expire-credentials, renew-credentials, credentialsNonExpired"
    })
    void statusFlagIsToggledAndReflectedInResponse(String turnOff, String turnOn, String field) throws Exception {
        long id = idOf("user");

        mockMvc.perform(post("/users/{id}/" + turnOff, id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/{id}", id))
                .andExpect(jsonPath("$." + field).value(false));

        mockMvc.perform(post("/users/{id}/" + turnOn, id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/{id}", id))
                .andExpect(jsonPath("$." + field).value(true));
    }

    @Test
    void statusChangeOnNonExistentUserReturnsNotFound() throws Exception {
        mockMvc.perform(post("/users/{id}/disable", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserThenFetchReturnsNotFound() throws Exception {
        long id = idOf("blocked");

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("User not found"));
    }

    @Test
    void deleteNonExistentUserReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/users/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    private long idOf(String username) throws Exception {
        String body = mockMvc.perform(get("/users/by-username/{username}", username))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
