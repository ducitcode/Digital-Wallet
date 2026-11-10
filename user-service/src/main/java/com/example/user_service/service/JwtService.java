package com.example.user_service.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails, Map<String, Object> extraClaims);
    String generateRefreshToken(UserDetails userDetails);
    String extractUsername(String token);
    boolean isValidToken(String token, UserDetails userDetails);
}