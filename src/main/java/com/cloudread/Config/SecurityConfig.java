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

    // ==== PUBLIC ENDPOINTS KHÔNG CẦN LOGIN ====
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/actuator/health",
            "/error"
    };

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(request -> request
                        // PUBLIC API → permitAll
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Các API public bằng GET
                        .requestMatchers(HttpMethod.GET, "/books/**").permitAll()

                        // Các API cần ADMIN
                        .requestMatchers(HttpMethod.GET).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")

                        // Mặc định các API còn lại yêu cầu JWT
                        .anyRequest().authenticated()
                )

                // Tắt CSRF trong REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Bật CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Bật Resource Server (JWT)
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(customJwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec keySpec = new SecretKeySpec(JWT_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(keySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("");  // Không thêm ROLE_
        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(converter);
        return authConverter;
    }
}
