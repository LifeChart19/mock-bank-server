package com.example.mockbank.application.service;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.domain.account.entity.Account;
import com.example.mockbank.domain.account.entity.Transaction;
import com.example.mockbank.domain.account.repository.AccountRepository;
import com.example.mockbank.domain.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

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

    public AccountResponse deposit(Long userId, DepositRequest request) {
        // 1. 계좌 조회
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 2. 금액 업데이트
        account.deposit(request.getAmount());
        accountRepository.save(account);

        // 3. 트랜잭션 기록 생성 (optional)
        Transaction tx = Transaction.builder()
                .account(account)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .type(Transaction.TransactionType.DEPOSIT)
                .memo(request.getMemo())
                .description("입금") // 필요하면
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        // 4. 반환
        return AccountResponse.from(account);
    }

    public AccountResponse withdraw(Long userId, WithdrawRequest request) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().longValue() < request.getAmount()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.withdraw(request.getAmount());
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .account(account)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .type(Transaction.TransactionType.WITHDRAWAL)
                .memo(request.getMemo())
                .description("출금")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return AccountResponse.from(account);
    }



    public AccountResponse getAccount(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return AccountResponse.from(account);
    }

    public List<TransactionResponse> getTransactions(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return account.getTransactions().stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
