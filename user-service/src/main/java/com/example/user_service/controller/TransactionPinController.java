package com.example.user_service.controller;

import com.example.user_service.dto.request.PinRequestDTO;
import com.example.user_service.dto.response.ApiResponseDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.TransactionPinService;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pin")
@RequiredArgsConstructor
public class TransactionPinController {
    private final TransactionPinService pinService;
    private final UserService userService;

    @PostMapping
    public ApiResponseDTO<Void> create(Authentication auth, @Valid @RequestBody PinRequestDTO request) {
        pinService.createPin(userId(auth), request);
        return ApiResponseDTO.success("Tạo PIN thành công", null);
    }

    @PutMapping
    public ApiResponseDTO<Void> change(Authentication auth, @Valid @RequestBody PinRequestDTO request) {
        pinService.changePin(userId(auth), request);
        return ApiResponseDTO.success("Đổi PIN thành công", null);
    }

    @PostMapping("/verify")
    public ApiResponseDTO<Boolean> verify(Authentication auth, @Valid @RequestBody PinRequestDTO request) {
        return ApiResponseDTO.success("Xác minh PIN thành công", pinService.verifyPin(userId(auth), request.getPin()));
    }

    private Long userId(Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        return user.getId();
    }
}
