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
import java.util.Random;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        // salary: null이면 200~500만 랜덤생성
        BigDecimal salary = request.getSalary();
        boolean isSalaryRandom = false;
        if (salary == null) {
            salary = BigDecimal.valueOf(new Random().nextInt(301) + 200)
                    .multiply(BigDecimal.valueOf(10000)); // 200~500만
            isSalaryRandom = true;
        }

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .salary(salary)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        account = accountRepository.save(account);

        // 거래내역 자동 생성
        List<Transaction> transactions = generateInitialTransactions(account, salary, isSalaryRandom);
        for (Transaction tx : transactions) {
            transactionRepository.save(tx);
        }

        // 잔액 계산/적용
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.DEPOSIT) balance = balance.add(tx.getAmount());
            else balance = balance.subtract(tx.getAmount());
            if (balance.compareTo(BigDecimal.ZERO) < 0) balance = BigDecimal.ZERO;
        }
        account.setBalance(balance);
        accountRepository.save(account);

        return AccountResponse.from(account);
    }

    // 거래내역 생성 로직
    private List<Transaction> generateInitialTransactions(Account account, BigDecimal salary, boolean isSalaryRandom) {
        List<Transaction> all = new ArrayList<>();
        Random rand = new Random();

        int monthCount = rand.nextInt(27) + 10; // 10~36개월
        LocalDateTime now = LocalDateTime.now();
        BigDecimal currentBalance = BigDecimal.ZERO;

        for (int i = 0; i < monthCount; i++) {
            LocalDateTime month = now.minusMonths(i);
            int txCount = rand.nextInt(11) + 10; // 10~20건
            int salaryIdx = rand.nextInt(txCount);

            for (int t = 0; t < txCount; t++) {
                TransactionType type;
                BigDecimal amount;

                if (t == salaryIdx) {
                    type = TransactionType.DEPOSIT;
                    amount = salary;
                } else {
                    type = rand.nextBoolean() ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
                    amount = BigDecimal.valueOf(rand.nextInt(700_001)); // 0~70만
                    if (amount.compareTo(BigDecimal.ZERO) == 0) amount = BigDecimal.valueOf(1000);
                }

                String memo = (t == salaryIdx)
                        ? (isSalaryRandom ? "수입" : "월급")
                        : (type == TransactionType.DEPOSIT ? "입금" : "출금");

                int day = (t == salaryIdx) ? 28 : rand.nextInt(27) + 1;
                LocalDateTime txDate = month.withDayOfMonth(Math.min(day, month.toLocalDate().lengthOfMonth()))
                        .withHour(rand.nextInt(10) + 8);

                // 음수 방지
                if (type == TransactionType.WITHDRAWAL && currentBalance.compareTo(amount) < 0) continue;

                Transaction tx = Transaction.builder()
                        .account(account)
                        .amount(amount)
                        .type(type)
                        .memo(memo)
                        .description(memo)
                        .createdAt(txDate)
                        .build();

                all.add(tx);
                if (type == TransactionType.DEPOSIT) currentBalance = currentBalance.add(amount);
                else currentBalance = currentBalance.subtract(amount);
            }
        }
        return all;
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

    public boolean existsByAccountNumber(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }
}
