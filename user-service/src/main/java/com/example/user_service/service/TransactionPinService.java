package com.example.user_service.service;

import com.example.user_service.dto.request.PinRequestDTO;

public interface TransactionPinService {
    void createPin(Long userId, PinRequestDTO request);
    void changePin(Long userId, PinRequestDTO request);
    boolean verifyPin(Long userId, String rawPin);
}