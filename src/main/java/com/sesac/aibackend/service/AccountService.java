package com.sesac.aibackend.service;

import com.sesac.aibackend.domain.Account;
import com.sesac.aibackend.dto.AccountCreateRequest;
import com.sesac.aibackend.dto.AccountResponse;
import com.sesac.aibackend.dto.AccountUpdateRequest;
import com.sesac.aibackend.error.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AccountService {

    private final AtomicLong sequence = new AtomicLong();

    public Account createAccount(AccountCreateRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        sb.append("000");
        sb.replace(0, sb.length(), Long.toString(Long.parseLong(sb.toString()) + (sequence.getAndIncrement() + 1)));
        String accountNumber = sb.toString();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountHolder(req.accountHolder())
                .balance(req.balance())
                .createdDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        return account;
    }

    public void depositAccount(AccountUpdateRequest req, Account account) {
        account.setBalance(account.getBalance() + req.balance());
        account.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    public void withdrawAccount(AccountUpdateRequest req, Account account) {
        account.setBalance(account.getBalance() - req.balance());
        account.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    public void deleteAccount(String accountNumber, Map<String, Account> storage){
        if (storage.remove(accountNumber) == null) {
            throw NotFoundException.of("account", accountNumber);
        }
    }

}
