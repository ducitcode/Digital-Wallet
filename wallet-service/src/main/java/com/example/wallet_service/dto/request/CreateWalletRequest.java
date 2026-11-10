package com.example.wallet_service.dto.request;

import jakarta.validation.constraints.Pattern;

public record CreateWalletRequest(
        @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a 3-letter code")
        String currency
) {
}
