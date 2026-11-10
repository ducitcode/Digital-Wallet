package com.example.user_service.service;

import com.example.user_service.dto.request.UpdateUserRequestDTO;
import com.example.user_service.dto.response.UserResponseDTO;
import com.example.user_service.entity.User;

public interface UserService {
    UserResponseDTO getCurrentUser(Long userId);
    UserResponseDTO updateCurrentUser(Long userId, UpdateUserRequestDTO request);
    User findById(Long id);
    User findByUsername(String username);
}
