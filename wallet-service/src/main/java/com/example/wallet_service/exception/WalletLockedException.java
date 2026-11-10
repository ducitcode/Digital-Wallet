package com.example.wallet_service.exception;

public class WalletLockedException extends RuntimeException {
    public WalletLockedException(String message) {
        super(message);
    }
}
