package com.sesac.aibackend.dto;

import java.time.LocalDateTime;

public record AccountResponse(String accountNumber, String accountHolder, Long balance,
                              LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
    public static AccountResponse from(com.sesac.aibackend.domain.Account account) {
        return new AccountResponse(account.getAccountNumber(), account.getAccountHolder(), account.getBalance(),
                account.getCreatedDate(), account.getLastUpdatedDate());
    }
}
