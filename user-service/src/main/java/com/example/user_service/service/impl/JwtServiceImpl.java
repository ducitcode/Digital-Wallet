package com.example.user_service.service.impl;

import com.example.user_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {
    private final SecretKey key;
    private final long accessTtl;
    private final long refreshTtl;

    public JwtServiceImpl(@Value("${app.jwt.secret}") String secret,
                          @Value("${app.jwt.access-token-expiration-ms:900000}") long accessTtl,
                          @Value("${app.jwt.refresh-token-expiration-ms:604800000}") long refreshTtl) {
        byte[] decoded = Decoders.BASE64.decode(secret);
        if (decoded.length < 32) throw new IllegalArgumentException("JWT secret must decode to at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(decoded);
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    @Override
    public String generateAccessToken(UserDetails details, Map<String, Object> claims) {
        return build(details.getUsername(), claims, accessTtl, "access");
    }

    @Override
    public String generateRefreshToken(UserDetails details) {
        return build(details.getUsername(), Map.of(), refreshTtl, "refresh");
    }

    private String build(String subject, Map<String, Object> claims, long ttl, String type) {
        Date now = new Date();
        return Jwts.builder().claims(claims).id(UUID.randomUUID().toString()).subject(subject).claim("token_type", type)
                .issuedAt(now).expiration(new Date(now.getTime() + ttl)).signWith(key).compact();
    }

    @Override
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    @Override
    public boolean isValidToken(String token, UserDetails details) {
        Claims claims = parse(token);
        return details.getUsername().equals(claims.getSubject()) && "access".equals(claims.get("token_type"));
    }

    public boolean isValidRefreshToken(String token, UserDetails details) {
        Claims claims = parse(token);
        return details.getUsername().equals(claims.getSubject()) && "refresh".equals(claims.get("token_type"));
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
