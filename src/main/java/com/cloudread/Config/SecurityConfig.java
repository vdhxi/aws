package com.cloudread.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.key}")
    private String JWT_KEY;

    // ===============================
    // PUBLIC ENDPOINTS (FULL & EXACT)
    // ===============================
    private static final String[] PUBLIC_ENDPOINTS = {

            // AUTH
            "/api/v1/auth/register",
            "/api/v1/auth/register/verify-email-exist",
            "/api/v1/auth/login",
            "/api/v1/auth/introspect",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/forget-password",
            "/api/v1/auth/forget-password/verify-otp",
            "/api/v1/auth/reset-password",

            // BOOK
            "/api/v1/book",
            "/api/v1/book/**",

            // AUTHOR
            "/api/v1/author",
            "/api/v1/author/**",

            // CATEGORY
            "/api/v1/category",
            "/api/v1/category/**",

            // PUBLIC TEST
            "/api/v1/public/**",

            // HEALTH CHECK
            "/api/v1/health",
            "/health",

            // Swagger
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",

            // Error handler
            "/error"
    };

    // ==========================
    // PASSWORD ENCODER
    // ==========================
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // ==========================
    // SECURITY FILTER CHAIN
    // ==========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                    // ADMIN ONLY ENDPOINTS
                    .requestMatchers(HttpMethod.POST).hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT).hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")

                    // All others require JWT
                    .anyRequest().authenticated()
            )

            // Disable CSRF (REST API)
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // JWT Resource server
            .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwt -> jwt
                            .decoder(jwtDecoder())
                            .jwtAuthenticationConverter(customJwtAuthenticationConverter())
                    )
            );

        return http.build();
    }

    // ==========================
    // CORS CONFIG
    // ==========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*")); // Allow FE everywhere
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", config);
        return src;
    }

    // ==========================
    // JWT DECODER
    // ==========================
    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(JWT_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    // ==========================
    // REMOVE ROLE_ PREFIX
    // ==========================
    @Bean
    JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(""); // Do not add ROLE_

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
