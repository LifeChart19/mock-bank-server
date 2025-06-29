package com.example.mockbank.application.service;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.domain.account.entity.Account;
import com.example.mockbank.domain.account.entity.Transaction;
import com.example.mockbank.domain.account.enums.TransactionType;
import com.example.mockbank.domain.account.repository.AccountRepository;
import com.example.mockbank.domain.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.mockbank.common.enums.ErrorCode;
import com.example.mockbank.common.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    @Transactional
    public AccountResponse deposit(Long userId, DepositRequest request) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        account.deposit(request.getAmount());
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .account(account)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .memo(request.getMemo())
                .description("입금")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse withdraw(Long userId, WithdrawRequest request) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        BigDecimal withdrawAmount = request.getAmount();
        if (account.getBalance().compareTo(withdrawAmount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        account.withdraw(request.getAmount());
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .account(account)
                .amount(withdrawAmount)
                .type(TransactionType.WITHDRAWAL)
                .memo(request.getMemo())
                .description("출금")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return AccountResponse.from(account);
    }


    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return account.getTransactions().stream()
                .map(TransactionResponse::from)
                .toList();
    }
    //
}
