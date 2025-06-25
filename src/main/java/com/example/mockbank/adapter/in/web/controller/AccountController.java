package com.example.mockbank.adapter.in.web.controller;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.application.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccount(userId));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getTransactions(userId));
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long userId,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(userId, request));
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long userId,
            @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(accountService.withdraw(userId, request));
    }

}
