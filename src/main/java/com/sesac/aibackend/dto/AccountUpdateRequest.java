package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Account;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AccountUpdateRequest(
        @NotBlank String accountNumber,
        @Min(1) Long balance
) {
    public Account toEntity() {
        return Account.builder().accountNumber(accountNumber).balance(balance).build();
    }
}
