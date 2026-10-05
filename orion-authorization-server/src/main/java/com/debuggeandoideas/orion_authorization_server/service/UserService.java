package com.debuggeandoideas.orion_authorization_server.service;

import com.debuggeandoideas.orion_authorization_server.dto.CreateUserRequest;
import com.debuggeandoideas.orion_authorization_server.dto.UpdatePasswordRequest;
import com.debuggeandoideas.orion_authorization_server.dto.UpdateRoleRequest;
import com.debuggeandoideas.orion_authorization_server.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    void delete(Long id);

    void updatePassword(Long id, UpdatePasswordRequest request);

    UserResponse updateRole(Long id, UpdateRoleRequest request);

    void setEnabled(Long id, boolean enabled);

    void setAccountNonLocked(Long id, boolean accountNonLocked);

    void setAccountNonExpired(Long id, boolean accountNonExpired);

    void setCredentialsNonExpired(Long id, boolean credentialsNonExpired);
}
