package com.debuggeandoideas.orion_authorization_server.service;

import com.debuggeandoideas.orion_authorization_server.dto.CreateUserRequest;
import com.debuggeandoideas.orion_authorization_server.dto.UpdatePasswordRequest;
import com.debuggeandoideas.orion_authorization_server.dto.UpdateRoleRequest;
import com.debuggeandoideas.orion_authorization_server.dto.UserResponse;
import com.debuggeandoideas.orion_authorization_server.entity.RoleEntity;
import com.debuggeandoideas.orion_authorization_server.entity.UserEntity;
import com.debuggeandoideas.orion_authorization_server.exception.RoleNotFoundException;
import com.debuggeandoideas.orion_authorization_server.exception.UserNotFoundException;
import com.debuggeandoideas.orion_authorization_server.exception.UsernameAlreadyExistsException;
import com.debuggeandoideas.orion_authorization_server.repository.RoleRepository;
import com.debuggeandoideas.orion_authorization_server.repository.UserRepository;
import com.debuggeandoideas.orion_authorization_server.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        String roleName = request.role() == null || request.role().isBlank() ? DEFAULT_ROLE : request.role();

        String psdHashedPassword
                = this.passwordEncoder.encode(request.password());

        UserEntity user = new UserEntity
                (request.username(), psdHashedPassword, findRole(roleName));
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(findUser(id));
    }

    @Override
    public UserResponse findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> UserNotFoundException.withUsername(username));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.delete(findUser(id));
    }

    @Override
    @Transactional
    public void updatePassword(Long id, UpdatePasswordRequest request) {
        findUser(id).setPassword(this.passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        UserEntity user = findUser(id);
        user.setRole(findRole(request.role()));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        findUser(id).setEnabled(enabled);
    }

    @Override
    @Transactional
    public void setAccountNonLocked(Long id, boolean accountNonLocked) {
        findUser(id).setAccountNonLocked(accountNonLocked);
    }

    @Override
    @Transactional
    public void setAccountNonExpired(Long id, boolean accountNonExpired) {
        findUser(id).setAccountNonExpired(accountNonExpired);
    }

    @Override
    @Transactional
    public void setCredentialsNonExpired(Long id, boolean credentialsNonExpired) {
        findUser(id).setCredentialsNonExpired(credentialsNonExpired);
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));
    }

    private RoleEntity findRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));
    }
}
