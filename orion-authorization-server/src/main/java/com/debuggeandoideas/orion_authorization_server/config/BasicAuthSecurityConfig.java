package com.debuggeandoideas.orion_authorization_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class BasicAuthSecurityConfig {

    private static final String USERS_PATH = "/users";
    private static final String USERS_SUB_PATHS = "/users/**";
    private static final String AUTH_ADMIN_ROLE = "AUTH_ADMIN";
    private static final int BASIC_AUTH_CHAIN_ORDER = 2;

    @Value("${orion.admin.username:AuthAdmin}")
    private String username;

    @Value("${orion.admin.password:secret1234}")
    private String password;

    @Bean(value = "basicAuthFilterChain")
    @Order(BASIC_AUTH_CHAIN_ORDER)
    SecurityFilterChain basicAuthFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {

        UserDetails authAdmin = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles(AUTH_ADMIN_ROLE)
                .build();
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager(authAdmin);

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsManager);
        provider.setPasswordEncoder(passwordEncoder);

        http
            .securityMatcher(USERS_PATH, USERS_SUB_PATHS)
            .authorizeHttpRequests(auth -> auth.anyRequest().hasRole(AUTH_ADMIN_ROLE))
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .authenticationManager(new ProviderManager(provider));

        return http.build();
    }
}
