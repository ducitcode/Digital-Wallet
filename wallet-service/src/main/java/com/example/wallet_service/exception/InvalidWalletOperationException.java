package com.example.wallet_service.exception;

public class InvalidWalletOperationException extends RuntimeException {
    public InvalidWalletOperationException(String message) {
        super(message);
    }
}
