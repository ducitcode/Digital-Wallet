package com.example.user_service.service.impl;

import com.example.user_service.entity.RefreshToken;
import com.example.user_service.exception.BusinessException;
import com.example.user_service.repository.RefreshTokenRepository;
import com.example.user_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository repository;
    @Value("${app.jwt.refresh-token-expiration-ms:604800000}") private long ttlMs;

    @Override @Transactional public RefreshToken createRefreshToken(Long userId, String rawToken) {
        return repository.save(RefreshToken.builder().userId(userId).tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusNanos(ttlMs * 1_000_000)).build());
    }
    @Override public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getRevokedAt() != null || !token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token hết hạn hoặc đã bị thu hồi");
        }
        return token;
    }
    @Override @Transactional public RefreshToken findValidToken(String rawToken) {
        return repository.findByTokenHashForUpdate(hash(rawToken)).map(this::verifyExpiration)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"));
    }
    @Override @Transactional public void revokeToken(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) { token.setRevokedAt(LocalDateTime.now()); repository.save(token); }
        });
    }
    @Override @Transactional public void revokeAllForUser(Long userId) {
        repository.findAllByUserIdAndRevokedAtIsNull(userId).forEach(t -> t.setRevokedAt(LocalDateTime.now()));
    }
    @Override @Transactional public void deleteByUserId(Long userId) { repository.deleteByUserId(userId); }
    private String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 is unavailable", ex); }
    }
}
