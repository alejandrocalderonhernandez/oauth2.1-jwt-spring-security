package com.debuggeandoideas.orion_resource_server.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${orion.auth.jwk.uri:http://localhost:9000/oauth2/jwks}")
    private String jwkSetUri;

    @Value("${orion.resource.audience:orion-resource-server}")
    private String resourceAudience;

    @Value("${orion.auth.issuer:http://localhost:9000}")
    private String issuer;


    private static final String AUTH_MISSION_READ = "SCOPE_mission.read";
    private static final String AUTH_MISSION_WRITE = "SCOPE_mission.write";
    private static final String API_NEWS = "/api/news";
    private static final String API_MISSIONS = "/api/missions";

    private static final String ROLES_CLAIM = "roles";
    private static final String NO_PREFIX = "";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_USER = "USER";

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String PROBLEM_BODY = "{\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}";
    private static final String DETAIL_UNAUTHORIZED = "A valid access token is required";
    private static final String DETAIL_FORBIDDEN = "The token does not grant access to this resource";
    private static final String BEARER = "Bearer";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationEntryPoint authenticationEntryPoint,
                                            AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .authorizeHttpRequests(auth ->
                                auth
                                    .requestMatchers(HttpMethod.GET, API_NEWS).permitAll()
                                    .requestMatchers(HttpMethod.GET, API_MISSIONS).access(
                                                AuthorizationManagers.allOf(
                                                        AuthorityAuthorizationManager.hasAuthority(AUTH_MISSION_READ),//
                                                        AuthorityAuthorizationManager.hasRole(ROLE_USER)
                                                )
                                        )
                                    .requestMatchers(HttpMethod.POST, API_MISSIONS).access(
                                                AuthorizationManagers.allOf(
                                                        AuthorityAuthorizationManager.hasAuthority(AUTH_MISSION_WRITE),
                                                        AuthorityAuthorizationManager.hasRole(ROLE_MANAGER)
                                                )
                                        )
                                    .anyRequest().authenticated()
                        )
                // Never create JSESSIONID
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }


    @Bean
    JwtDecoder jwtDecoder() {
        var decoder =  NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO),
                new JwtIssuerValidator(issuer),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(resourceAudience)
                )
        ));

        return decoder;

    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes =  new JwtGrantedAuthoritiesConverter();
        JwtGrantedAuthoritiesConverter roles =  new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(ROLES_CLAIM);
        roles.setAuthorityPrefix(NO_PREFIX);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt ->{
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            authorities.addAll(roles.convert(jwt));

            return authorities;
        });

        return converter;
    }

    @Bean
    BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver defaultBearerTokenResolver =  new DefaultBearerTokenResolver();
        RequestMatcher publicRoute = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, API_NEWS);
        return req -> publicRoute.matches(req) ? null : defaultBearerTokenResolver.resolve(req);
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) -> {
          res.setHeader(HttpHeaders.WWW_AUTHENTICATE, BEARER);
          this.writeProblem(res, HttpStatus.UNAUTHORIZED, DETAIL_UNAUTHORIZED);
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) ->
                this.writeProblem(res, HttpStatus.FORBIDDEN, DETAIL_FORBIDDEN);
    }


    private void writeProblem(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(PROBLEM_JSON);
        response.getWriter().write(PROBLEM_BODY.formatted(status.getReasonPhrase(), status.value(), detail));
    }
}
