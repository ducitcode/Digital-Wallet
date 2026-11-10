package com.example.user_service.controller;

import com.example.user_service.dto.request.*;
import com.example.user_service.dto.response.ApiResponseDTO;
import com.example.user_service.dto.response.AuthResponseDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.AuthService;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ApiResponseDTO.success("Đăng ký thành công", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponseDTO<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ApiResponseDTO.success("Đăng nhập thành công", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponseDTO<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ApiResponseDTO.success("Làm mới token thành công", authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponseDTO<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return ApiResponseDTO.success("Đăng xuất thành công", null);
    }

    @PostMapping("/change-password")
    public ApiResponseDTO<Void> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequestDTO request) {
        User user = userService.findByUsername(auth.getName());
        authService.changePassword(user.getId(), request);
        return ApiResponseDTO.success("Đổi mật khẩu thành công; vui lòng đăng nhập lại", null);
    }
}
