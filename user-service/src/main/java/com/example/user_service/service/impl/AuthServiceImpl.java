package com.example.user_service.service.impl;

import com.example.user_service.dto.request.*;
import com.example.user_service.dto.response.AuthResponseDTO;
import com.example.user_service.dto.response.UserResponseDTO;
import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.User;
import com.example.user_service.enums.UserRole;
import com.example.user_service.enums.UserStatus;
import com.example.user_service.exception.BusinessException;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.AuthService;
import com.example.user_service.service.JwtService;
import com.example.user_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtServiceImpl jwtServiceImpl;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByUsername(username))
            throw new BusinessException(HttpStatus.CONFLICT, "Username đã tồn tại");
        if (userRepository.existsByEmail(email)) throw new BusinessException(HttpStatus.CONFLICT, "Email đã tồn tại");
        User user = userRepository.save(User.builder().username(username).email(email).phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword())).fullName(request.getFullName().trim())
                .role(UserRole.USER).status(UserStatus.ACTIVE).emailVerified(false).build());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        String login = request.getUsernameOrEmail().trim();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập/email hoặc mật khẩu không đúng");
        }
        User user = userRepository.findByUsernameOrEmail(login, login.toLowerCase())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không hợp lệ"));
        ensureActive(user);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String raw = request.getRefreshToken();
        RefreshToken stored = refreshTokenService.findValidToken(raw);
        User user = userRepository.findById(stored.getUserId()).orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        String username;
        try {
            username = jwtService.extractUsername(raw);
            if (!jwtServiceImpl.isValidRefreshToken(raw, principal(user)))
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ");
        } catch (RuntimeException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ");
        }
        ensureActive(user);
        if (!user.getUsername().equals(username))
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ");
        refreshTokenService.revokeToken(raw);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu hiện tại");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenService.revokeAllForUser(userId);
    }

    private AuthResponseDTO issueTokens(User user) {
        UserDetails principal = principal(user);
        String access = jwtService.generateAccessToken(principal, Map.of("userId", user.getId(), "role", user.getRole().name()));
        String refresh = jwtService.generateRefreshToken(principal);
        refreshTokenService.createRefreshToken(user.getId(), refresh);
        return AuthResponseDTO.builder().accessToken(access).refreshToken(refresh).user(map(user)).build();
    }

    private UserDetails principal(User user) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword()).authorities("ROLE_" + user.getRole().name()).build();
    }

    private void ensureActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE)
            throw new BusinessException(HttpStatus.FORBIDDEN, "Tài khoản không hoạt động");
    }

    private UserResponseDTO map(User user) {
        return UserResponseDTO.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .phone(user.getPhone()).fullName(user.getFullName()).role(user.getRole()).status(user.getStatus())
                .emailVerified(user.getEmailVerified()).createdAt(user.getCreatedAt()).build();
    }
}
