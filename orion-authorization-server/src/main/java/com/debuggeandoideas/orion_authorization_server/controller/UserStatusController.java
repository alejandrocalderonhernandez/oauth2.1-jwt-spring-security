package com.debuggeandoideas.orion_authorization_server.controller;

import com.debuggeandoideas.orion_authorization_server.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{id}")
public class UserStatusController {

    private final UserService userService;

    public UserStatusController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        userService.setEnabled(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        userService.setEnabled(id, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lock")
    public ResponseEntity<Void> lock(@PathVariable Long id) {
        userService.setAccountNonLocked(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unlock")
    public ResponseEntity<Void> unlock(@PathVariable Long id) {
        userService.setAccountNonLocked(id, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expire(@PathVariable Long id) {
        userService.setAccountNonExpired(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/renew")
    public ResponseEntity<Void> renew(@PathVariable Long id) {
        userService.setAccountNonExpired(id, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/expire-credentials")
    public ResponseEntity<Void> expireCredentials(@PathVariable Long id) {
        userService.setCredentialsNonExpired(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/renew-credentials")
    public ResponseEntity<Void> renewCredentials(@PathVariable Long id) {
        userService.setCredentialsNonExpired(id, true);
        return ResponseEntity.noContent().build();
    }
}
