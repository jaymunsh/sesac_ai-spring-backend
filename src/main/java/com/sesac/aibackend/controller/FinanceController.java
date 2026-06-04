package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Account;
import com.sesac.aibackend.dto.AccountCreateRequest;
import com.sesac.aibackend.dto.AccountResponse;
import com.sesac.aibackend.dto.AccountUpdateRequest;
import com.sesac.aibackend.error.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finance")
public class FinanceController {

    private final Map<String, Account> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Operation(summary = "전체 계좌조회")
    @GetMapping("/account/all")
    public List<AccountResponse> getAllAccountList(){
        return storage.values().stream().map(AccountResponse::from).toList();
    }

    @Operation(summary = "특정 계좌조회")
    @GetMapping("/account/{accountNumber}")
    public AccountResponse getAccount(@PathVariable String accountNumber) {
        Account account = storage.get(accountNumber);
        return AccountResponse.from(account);
    }

    @Operation(summary = "계좌 생성")
    @PostMapping("/account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest req) {
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

        storage.put(accountNumber, account);

        return ResponseEntity.created(URI.create("/finance/account/" + accountNumber)).body(AccountResponse.from(account));
    }

    @Operation(summary = "입금")
    @PutMapping("/account/deposit/{accountNumber}")
    public AccountResponse depositAccount(@PathVariable String accountNumber, @Valid @RequestBody AccountUpdateRequest req) {
        Account account = storage.get(accountNumber);
        if (account == null) {
            throw NotFoundException.of("account", accountNumber);
        }
        account.setBalance(account.getBalance() + req.balance());
        account.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        return AccountResponse.from(account);
    }

    @Operation(summary = "출금")
    @PutMapping("/account/withdraw/{accountNumber}")
    public AccountResponse withdrawAccount(@PathVariable String accountNumber, @Valid @RequestBody AccountUpdateRequest req) {
        Account account = storage.get(accountNumber);
        if (account == null) {
            throw NotFoundException.of("account", accountNumber);
        }
        account.setBalance(account.getBalance() - req.balance());
        account.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        return AccountResponse.from(account);
    }

    @Operation(summary = "계좌 삭제")
    @DeleteMapping("/account/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        if (storage.remove(accountNumber) == null) {
            throw NotFoundException.of("account", accountNumber);
        }
        return ResponseEntity.noContent().build();
    }




}
