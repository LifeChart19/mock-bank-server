package com.example.mockbank.adapter.in.web.controller;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.application.service.AccountService;
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

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getTransactions(accountNumber));
    }
}
