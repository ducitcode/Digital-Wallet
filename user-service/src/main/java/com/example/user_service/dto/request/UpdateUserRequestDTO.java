package com.example.user_service.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String fullName;
    private String phone;
}