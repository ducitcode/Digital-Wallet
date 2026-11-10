package com.example.wallet_service.dto.response;

import com.example.wallet_service.entity.Wallet;

import java.math.BigDecimal;

public record WalletBalanceResponse(String walletNumber, BigDecimal balance, String currency) {
    public static WalletBalanceResponse from(Wallet wallet) {
        return new WalletBalanceResponse(wallet.getWalletNumber(), wallet.getBalance(), wallet.getCurrency());
    }
}
