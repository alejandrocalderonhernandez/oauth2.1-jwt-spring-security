package com.debuggeandoideas.orion_authorization_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean(value = "baseFilterChain")
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/users", "/users/**", "/h2-console/**").permitAll()
                            .anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/users/**", "/h2-console/**"))
            .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
