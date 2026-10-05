package com.debuggeandoideas.orion_authorization_server.config;

import com.debuggeandoideas.orion_authorization_server.entity.RoleEntity;
import com.debuggeandoideas.orion_authorization_server.entity.UserEntity;
import com.debuggeandoideas.orion_authorization_server.repository.RoleRepository;
import com.debuggeandoideas.orion_authorization_server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        RoleEntity admin = ensureRole("ROLE_ADMIN");
        RoleEntity manager = ensureRole("ROLE_MANAGER");
        RoleEntity user = ensureRole("ROLE_USER");

        ensureUser("admin", "admin1234", admin, true);
        ensureUser("manager", "manager1234", manager, true);
        ensureUser("user", "user1234", user, true);
        ensureUser("blocked", "blocked1234", user, false);

        log.info("Seeded {} roles and {} users", roleRepository.count(), userRepository.count());
    }

    private RoleEntity ensureRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new RoleEntity(name)));
    }

    private void ensureUser(String username, String password, RoleEntity role, boolean enabled) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        UserEntity user = new UserEntity(username, password, role);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
