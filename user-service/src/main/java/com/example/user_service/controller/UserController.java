package com.example.user_service.controller;

import com.example.user_service.dto.request.UpdateUserRequestDTO;
import com.example.user_service.dto.response.ApiResponseDTO;
import com.example.user_service.dto.response.UserResponseDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ApiResponseDTO<UserResponseDTO> getMe(Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        return ApiResponseDTO.success("Lấy thông tin thành công", userService.getCurrentUser(user.getId()));
    }

    @PutMapping
    public ApiResponseDTO<UserResponseDTO> updateMe(Authentication auth, @RequestBody UpdateUserRequestDTO request) {
        User user = userService.findByUsername(auth.getName());
        return ApiResponseDTO.success("Cập nhật thông tin thành công", userService.updateCurrentUser(user.getId(), request));
    }
}
