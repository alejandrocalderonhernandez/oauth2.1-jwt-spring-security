package com.debuggeandoideas.orion_authorization_server.config;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Configuration
public class AuthorizationServerConfig {

    @Value("${orion.auth.issuer:http://localhost:9000}")
    private String issuer;

    @Value("${orion.client.id:orion-frontend}")
    private String clientId;

    @Value("${orion.client.redirect-uri:http://localhost:5173/}")
    private String redirectUri;

    @Value("${orion.client.scopes:mission.read,mission.write}")
    private List<String> scopes;

    @Value("${orion.jwt.public-key:classpath:keys/public.pem}")
    private Resource publicKeyResource;

    @Value("${orion.jwt.private-key:classpath:keys/private.pem}")
    private Resource privateKeyResource;

    @Value("${orion.jwt.key-id:orion-key-1}")
    private String keyId;

    @Value("${orion.token.access.ttl-minutes:120}")
    private int accessTokenTTLMinutes;

    @Value("${orion.token.refresh.ttl-hours:8}")
    private int refreshTokenTTLHours;

    @Value("${orion.backend.client-id:orion-backend}")
    private String backendClientId;

    @Value("${orion.backend.client-secret:orion-secret}")
    private String backendClientSecret;

    @Value("${orion.backend.redirect-uri:https://oauth.pstmn.io/v1/callback}")
    private String backendRedirectUri;

    @Value("${orion.backend.id:orion-backend-id}")
    private String orionBackendId;


    @Bean(value = "authorizationServerFilterChain")
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .oauth2AuthorizationServer(as -> {
                    http.securityMatcher(as.getEndpointsMatcher());
                    as.oidc(Customizer.withDefaults());
                })

                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())

                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                ));
        return http.build();
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {

        RegisteredClient orionFrontend = RegisteredClient.withId(clientId + "-id")
                .clientId(clientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scopes(s -> s.addAll(scopes))
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTTLMinutes))
                        .refreshTokenTimeToLive(Duration.ofHours(refreshTokenTTLHours))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        RegisteredClient orionBackend = RegisteredClient.withId(orionBackendId)
                .clientId(backendClientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientSecret(Objects.requireNonNull(passwordEncoder.encode(backendClientSecret)))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(backendRedirectUri)
                .scopes(s -> s.addAll(scopes))
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(false)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTTLMinutes))
                        .refreshTokenTimeToLive(Duration.ofHours(refreshTokenTTLHours))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(orionFrontend, orionBackend);

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(redirectUri));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/oauth2/**", configuration);
        source.registerCorsConfiguration("/.well-known/**", configuration);

        return source;
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() throws IOException {

        RSAPublicKey publicKey = RsaKeyConverters.x509().convert(publicKeyResource.getInputStream());
        RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(privateKeyResource.getInputStream());

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

}
