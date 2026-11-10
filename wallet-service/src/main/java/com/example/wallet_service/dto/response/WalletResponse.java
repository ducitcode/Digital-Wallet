package com.example.wallet_service.dto.response;

import com.example.wallet_service.entity.Wallet;
import com.example.wallet_service.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String walletNumber,
        UUID userId,
        BigDecimal balance,
        String currency,
        WalletStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getWalletNumber(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus(),
                wallet.getVersion(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
