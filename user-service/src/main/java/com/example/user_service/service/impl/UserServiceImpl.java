package com.example.user_service.service.impl;

import com.example.user_service.dto.request.UpdateUserRequestDTO;
import com.example.user_service.dto.response.UserResponseDTO;
import com.example.user_service.entity.User;
import com.example.user_service.exception.BusinessException;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserResponseDTO getCurrentUser(Long userId) {
        return toDto(findById(userId));
    }

    @Override
    public User findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    @Override
    public User findByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    @Override
    @Transactional
    public UserResponseDTO updateCurrentUser(Long userId, UpdateUserRequestDTO request) {
        User user = findById(userId);
        if (request.getFullName() != null) {
            if (request.getFullName().isBlank() || request.getFullName().length() > 100)
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Họ tên không hợp lệ");
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            if (request.getPhone().length() > 20)
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ");
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
        return toDto(user);
    }

    private UserResponseDTO toDto(User u) {
        return UserResponseDTO.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).phone(u.getPhone()).fullName(u.getFullName()).role(u.getRole()).status(u.getStatus()).emailVerified(u.getEmailVerified()).createdAt(u.getCreatedAt()).build();
    }
}
