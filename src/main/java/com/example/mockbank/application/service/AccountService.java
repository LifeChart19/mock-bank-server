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
import java.time.YearMonth;
import java.util.Comparator;
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
        BigDecimal salary = request.getSalary();
        boolean isSalaryRandom = false;
        if (salary == null) {
            isSalaryRandom = true;
            salary = null; // 계좌에 null 저장 (거래내역은 별도로 랜덤생성)
        }

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .salary(salary) // null 가능!
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        List<Transaction> transactions = generateInitialTransactions(account, salary, isSalaryRandom);
        for (Transaction tx : transactions) transactionRepository.save(tx);

        // balance 재계산 (최신 거래까지)
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction tx : transactions.stream().sorted(Comparator.comparing(Transaction::getCreatedAt)).toList()) {
            if (tx.getType() == TransactionType.DEPOSIT) balance = balance.add(tx.getAmount());
            else balance = balance.subtract(tx.getAmount());
            if (balance.compareTo(BigDecimal.ZERO) < 0) balance = BigDecimal.ZERO;
        }
        account.setBalance(balance);
        accountRepository.save(account);

        return AccountResponse.from(account);
    }


    private List<Transaction> generateInitialTransactions(Account account, BigDecimal salary, boolean isSalaryRandom) {
        List<Transaction> all = new ArrayList<>();
        Random rand = new Random();
        LocalDateTime now = LocalDateTime.now();
        int currentDay = now.getDayOfMonth();
        int monthCount = rand.nextInt(27) + 10; // 10~36개월
        BigDecimal currentBalance = BigDecimal.ZERO;

        for (int i = monthCount - 1; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            int lastDay = month.toLocalDate().lengthOfMonth();
            boolean isCurrentMonth = (i == 0);
            int txCount = isCurrentMonth ? Math.max(1, currentDay / 2) : rand.nextInt(11) + 10;

            List<Integer> daysPool = new ArrayList<>();
            for (int d = 1; d <= lastDay; d++) daysPool.add(d);
            daysPool.remove(Integer.valueOf(lastDay));
            java.util.Collections.shuffle(daysPool);

            List<Integer> sortedDays = daysPool.subList(0, Math.min(txCount - 1, daysPool.size()));
            java.util.Collections.sort(sortedDays);

            // 거래 생성
            for (int day : sortedDays) {
                LocalDateTime txDate = month.withDayOfMonth(day).withHour(rand.nextInt(10) + 8);
                if (isCurrentMonth && txDate.getDayOfMonth() >= currentDay) continue;
                TransactionType type = rand.nextBoolean() ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
                BigDecimal amount = BigDecimal.valueOf(rand.nextInt(700_001));
                if (amount.compareTo(BigDecimal.ZERO) == 0) amount = BigDecimal.valueOf(1000);
                if (type == TransactionType.WITHDRAWAL && currentBalance.compareTo(amount) < 0) continue;
                String memo = type == TransactionType.DEPOSIT ? "입금" : "출금";
                String desc = type == TransactionType.DEPOSIT ? "입금" : "출금";

                Transaction tx = Transaction.builder()
                        .account(account)
                        .amount(amount)
                        .type(type)
                        .memo(memo)
                        .description(desc)
                        .createdAt(txDate)
                        .build();

                all.add(tx);
                if (type == TransactionType.DEPOSIT) currentBalance = currentBalance.add(amount);
                else currentBalance = currentBalance.subtract(amount);
            }

            // 월급/수입 (월 말일)
            LocalDateTime salaryDate = month.withDayOfMonth(lastDay).withHour(rand.nextInt(10) + 8);
            boolean shouldAddSalary = !(isCurrentMonth && lastDay > currentDay);
            if (shouldAddSalary) {
                BigDecimal monthSalary = salary;
                String memo = "월급";
                if (isSalaryRandom) {
                    // 매달 200~500만 랜덤
                    monthSalary = BigDecimal.valueOf(rand.nextInt(301) + 200)
                            .multiply(BigDecimal.valueOf(10000));
                    memo = "수입";
                }
                Transaction tx = Transaction.builder()
                        .account(account)
                        .amount(monthSalary)
                        .type(TransactionType.DEPOSIT)
                        .memo(memo)
                        .description("입금")
                        .createdAt(salaryDate)
                        .build();
                all.add(tx);
                currentBalance = currentBalance.add(monthSalary);
            }
        }
        all.sort(Comparator.comparing(Transaction::getCreatedAt));
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
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionStatResponse getTransactionStats(Long userId, YearMonth startYm, YearMonth endYm) {

        if (startYm == null || endYm == null) {
            throw new CustomException(ErrorCode.INVALID_DATE_REQUEST);
        }

        if (startYm.isAfter(endYm)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 1. 거래 내역 필터링
        List<Transaction> txs = account.getTransactions().stream()
                .filter(tx -> {
                    java.time.YearMonth ym = java.time.YearMonth.from(tx.getCreatedAt());
                    return (ym.compareTo(startYm) >= 0 && ym.compareTo(endYm) <= 0);
                }).toList();

        // 2. 월별로 분리
        int monthCount = endYm.compareTo(startYm) + 1;

        // 3. 합계/평균 계산
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Transaction tx : txs) {
            if (tx.getType() == TransactionType.DEPOSIT) totalIncome = totalIncome.add(tx.getAmount());
            else if (tx.getType() == TransactionType.WITHDRAWAL) totalExpense = totalExpense.add(tx.getAmount());
        }

        BigDecimal avgIncome = (monthCount > 0) ? totalIncome.divide(BigDecimal.valueOf(monthCount), 0, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;
        BigDecimal avgExpense = (monthCount > 0) ? totalExpense.divide(BigDecimal.valueOf(monthCount), 0, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;

        return TransactionStatResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .avgMonthlyIncome(avgIncome)
                .avgMonthlyExpense(avgExpense)
                .build();
    }

    public boolean existsByAccountNumber(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }
}
