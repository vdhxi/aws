package com.cloudread.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EndpointConfig {

    // GET PUBLIC ENDPOINTS
    public static String[] GET_PUBLIC_ENDPOINTS = {
            // Author endpoints
            "/author",
            "/author/{authorId}",

            // Book endpoints
            "/book",
            "/book/{bookId}",
            "/book/search",
            "/book/newest",
            "/book/most-favorite",
            "/book/author/{authorId}/books",
            "/book/category/{categoryId}/books",

            // Category endpoints
            "/category",
            "/category/{categoryId}"
    };

    // POST PUBLIC ENDPOINTS
    public static String[] POST_PUBLIC_ENDPOINTS = {
            "/auth/register/verify-email-exist",
            "/auth/register",
            "/auth/login",
            "/auth/introspect",
            "/auth/refresh",
            "/auth/logout",
            "/auth/forget-password",
            "/auth/forget-password/verify-otp"
    };

    // PUT PUBLIC ENDPOINTS
    public static String[] PUT_PUBLIC_ENDPOINTS = {
            "/auth/reset-password"
    };

    // GENERAL PUBLIC ENDPOINTS (không giới hạn METHOD)
    public static String[] PUBLIC_ENDPOINTS = {
            "/healthz",          // Healthcheck — cho phép mọi method
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };
}
