package com.example.user_service.service;

import com.example.user_service.dto.request.ChangePasswordRequestDTO;
import com.example.user_service.dto.request.LoginRequestDTO;
import com.example.user_service.dto.request.RefreshTokenRequestDTO;
import com.example.user_service.dto.request.RegisterRequestDTO;
import com.example.user_service.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
    void logout(String refreshToken);
    void changePassword(Long userId, ChangePasswordRequestDTO request);
}