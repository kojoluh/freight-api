package com.fkluh.freight.v1.controller;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Operation(
        summary = "Authenticate and get JWT token",
        description = "Authenticates a user and returns a JWT token. Use this token as 'Bearer' in the Authorization header for other endpoints.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"username\": \"admin\", \"password\": \"password\"}")
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "JWT token returned",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"token\": \"<jwt>\"}")
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject(value = "Invalid credentials")
                )
            )
        }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@org.springframework.web.bind.annotation.RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                Key key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
                String token = Jwts.builder()
                        .setSubject(username)
                        .claim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                        .signWith(key, SignatureAlgorithm.HS256)
                        .compact();
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
} 