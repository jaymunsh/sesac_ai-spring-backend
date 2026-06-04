package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Account;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AccountCreateRequest(
        @NotBlank String accountHolder,
        @Min(0) Long balance
) {
    public Account toEntity() {
        return Account.builder().accountHolder(accountHolder).balance(balance).build();
    }
}
