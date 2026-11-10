package com.example.user_service.dto.response;

import com.example.user_service.enums.UserRole;
import com.example.user_service.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}