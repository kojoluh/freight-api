package com.fkluh.freight.v1.config;

import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestJwtDecoderConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> new Jwt(
            token,
            null,
            null,
            Map.of("alg", "HS256"),
            Map.of("sub", "admin", "roles", "ADMIN")
        );
    }
} 