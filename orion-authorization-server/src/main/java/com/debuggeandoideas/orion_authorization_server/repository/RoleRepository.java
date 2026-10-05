package com.debuggeandoideas.orion_authorization_server.repository;

import com.debuggeandoideas.orion_authorization_server.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);
}
