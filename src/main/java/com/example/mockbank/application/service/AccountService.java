package com.example.mockbank.application.service;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.domain.account.entity.Account;
import com.example.mockbank.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    // 내부 공통 로직으로 계좌 생성
    private Account createAccountInternal(Long userId, String userName) {
        return Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(userId)
                .userName(userName)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 일반 요청으로 계좌 생성
    public AccountResponse createAccount(AccountCreateRequest request) {
        Account account = createAccountInternal(request.getUserId(), request.getUserName());
        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    // 이벤트 기반 계좌 생성
    public void createAccountFromEvent(AccountCreatedEvent event) {
        Account account = createAccountInternal(event.getUserId(), event.getUserName());
        accountRepository.save(account);
        log.info("계좌 생성 완료 for userId={}", event.getUserId());
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
        return List.of(
                new TransactionResponse("DEPOSIT", new BigDecimal("100000"), LocalDateTime.now().minusDays(2)),
                new TransactionResponse("WITHDRAWAL", new BigDecimal("30000"), LocalDateTime.now().minusDays(1))
        );
    }
}
