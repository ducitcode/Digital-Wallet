package com.example.user_service.service.impl;

import com.example.user_service.dto.request.PinRequestDTO;
import com.example.user_service.entity.TransactionPin;
import com.example.user_service.exception.BusinessException;
import com.example.user_service.repository.TransactionPinRepository;
import com.example.user_service.service.TransactionPinService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class TransactionPinServiceImpl implements TransactionPinService {
    private final TransactionPinRepository repository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.jwt.pin-max-failed-attempts:5}") private int maxAttempts;
    @Value("${app.jwt.pin-lock-minutes:15}") private int lockMinutes;

    @Override @Transactional public void createPin(Long userId, PinRequestDTO request) {
        if (repository.findByUserIdForUpdate(userId).isPresent()) throw new BusinessException(HttpStatus.CONFLICT, "PIN đã được tạo");
        repository.save(TransactionPin.builder().userId(userId).pinHash(passwordEncoder.encode(request.getPin())).failedAttempts(0).build());
    }
    @Override @Transactional(noRollbackFor = BusinessException.class) public void changePin(Long userId, PinRequestDTO request) {
        TransactionPin pin = repository.findByUserIdForUpdate(userId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Chưa tạo PIN giao dịch"));
        checkUnlocked(pin);
        if (request.getOldPin() == null || !passwordEncoder.matches(request.getOldPin(), pin.getPinHash())) {
            recordFailure(pin);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "PIN hiện tại không đúng");
        }
        pin.setPinHash(passwordEncoder.encode(request.getPin())); pin.setFailedAttempts(0); pin.setLockedUntil(null);
    }
    @Override @Transactional(noRollbackFor = BusinessException.class) public boolean verifyPin(Long userId, String rawPin) {
        TransactionPin pin = repository.findByUserIdForUpdate(userId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Chưa tạo PIN giao dịch"));
        checkUnlocked(pin);
        if (!passwordEncoder.matches(rawPin, pin.getPinHash())) { recordFailure(pin); throw new BusinessException(HttpStatus.UNAUTHORIZED, "PIN không đúng"); }
        pin.setFailedAttempts(0); pin.setLockedUntil(null);
        return true;
    }
    private void checkUnlocked(TransactionPin pin) {
        if (pin.getLockedUntil() != null) {
            if (pin.getLockedUntil().isAfter(LocalDateTime.now())) throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "PIN đang bị khóa, vui lòng thử lại sau");
            pin.setLockedUntil(null); pin.setFailedAttempts(0);
        }
    }
    private void recordFailure(TransactionPin pin) {
        int attempts = pin.getFailedAttempts() + 1;
        pin.setFailedAttempts(attempts);
        if (attempts >= maxAttempts) pin.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
    }
}
