package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Account;
import com.sesac.aibackend.dto.AccountCreateRequest;
import com.sesac.aibackend.dto.AccountResponse;
import com.sesac.aibackend.dto.AccountUpdateRequest;
import com.sesac.aibackend.error.NotFoundException;
import com.sesac.aibackend.service.AccountService;
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
@RequestMapping("/account")
public class AccountController {

    private final Map<String, Account> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    private final AccountService accountService;

    @Operation(summary = "전체 계좌조회")
    @GetMapping("/list")
    public List<AccountResponse> getAccountList(){
        return storage.values().stream().map(AccountResponse::from).toList();
    }

    @Operation(summary = "특정 계좌조회")
    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(@PathVariable String accountNumber) {
        Account account = storage.get(accountNumber);
        return AccountResponse.from(account);
    }

    @Operation(summary = "계좌 생성")
    @PostMapping()
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest req) {

        Account account = accountService.createAccount(req); // 일단 DB가 없으니
        String accountNumber = account.getAccountNumber();

        storage.put(accountNumber, account);

        return ResponseEntity.created(URI.create("/finance/account/" + accountNumber)).body(AccountResponse.from(account));
    }

    @Operation(summary = "입금")
    @PutMapping("/deposit")
    public AccountResponse depositAccount(@Valid @RequestBody AccountUpdateRequest req) {
        Account account = storage.get(req.accountNumber());
        if (account == null) {
            throw NotFoundException.of("account", req.accountNumber());
        }
        accountService.depositAccount(req, account);

        return AccountResponse.from(account);
    }

    @Operation(summary = "출금")
    @PutMapping("/withdraw")
    public AccountResponse withdrawAccount(@Valid @RequestBody AccountUpdateRequest req) {
        Account account = storage.get(req.accountNumber());
        if (account == null) {
            throw NotFoundException.of("account", req.accountNumber());
        }
        accountService.withdrawAccount(req, account);

        return AccountResponse.from(account);
    }

    @Operation(summary = "계좌 삭제")
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber, storage);
        return ResponseEntity.noContent().build();
    }

}
