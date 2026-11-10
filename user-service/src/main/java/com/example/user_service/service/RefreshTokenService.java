package com.example.user_service.service;

import com.example.user_service.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId, String rawToken);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUserId(Long userId);
    void revokeToken(String rawToken);
    RefreshToken findValidToken(String rawToken);
    void revokeAllForUser(Long userId);
}
