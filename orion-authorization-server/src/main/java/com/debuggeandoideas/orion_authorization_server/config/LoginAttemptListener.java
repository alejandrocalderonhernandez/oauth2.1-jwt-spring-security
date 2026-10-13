package com.debuggeandoideas.orion_authorization_server.config;

import com.debuggeandoideas.orion_authorization_server.repository.UserRepository;
import com.debuggeandoideas.orion_authorization_server.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptListener {

    private static final String MAX_FAILED_ATTEMPTS_PROPERTY = "${orion.security.max-failed-attempts:3}";
    private static final int ONE_ATTEMPT = 1;

    @Value(MAX_FAILED_ATTEMPTS_PROPERTY)
    private int maxFailedAttempts;

    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();

    private final UserService userService;
    private final UserRepository userRepository;

    public LoginAttemptListener(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        // Spring hides UsernameNotFoundException as BadCredentials, so skip unknown users
        if (!userRepository.existsByUsername(username)) {
            return;
        }
        int attempts = failedAttempts.merge(username, ONE_ATTEMPT, Integer::sum);
        if (attempts >= maxFailedAttempts) {
            userService.lockByUsername(username);
            failedAttempts.remove(username);
        }
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        failedAttempts.remove(event.getAuthentication().getName());
    }
}
