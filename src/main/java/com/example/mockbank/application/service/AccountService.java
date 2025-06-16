package com.example.mockbank.application.service;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.domain.account.entity.Account;
import com.example.mockbank.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountResponse createAccount(AccountCreateRequest request) {
        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .userId(request.getUserId())
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return AccountResponse.from(account);
    }

    public BalanceResponse getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return new BalanceResponse(account.getAccountNumber(), account.getBalance());
    }

    public List<TransactionResponse> getTransactions(String accountNumber) {
        // 실제 DB 저장은 안 하지만, 가상의 거래내역 생성
        return List.of(
                new TransactionResponse("DEPOSIT", new BigDecimal("100000"), LocalDateTime.now().minusDays(2)),
                new TransactionResponse("WITHDRAWAL", new BigDecimal("30000"), LocalDateTime.now().minusDays(1))
        );
    }
}
