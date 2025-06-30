package com.example.mockbank.application.service;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.common.enums.ErrorCode;
import com.example.mockbank.common.exception.CustomException;
import com.example.mockbank.domain.account.entity.Account;
import com.example.mockbank.domain.account.entity.Transaction;
import com.example.mockbank.domain.account.repository.AccountRepository;
import com.example.mockbank.domain.account.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_success() {
        // given
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountNumber("1234567890");
        request.setUserId(1L);
        request.setUserName("테스터");

        given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("입금 성공")
    void deposit_success() {
        // given
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(10000L));
        request.setMemo("입금메모");

        Account account = Account.builder()
                .userId(1L)
                .userName("테스터")
                .accountNumber("1234567890")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(accountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        given(accountRepository.save(any(Account.class))).willReturn(account);

        // when
        AccountResponse response = accountService.deposit(1L, request);

        // then
        assertThat(response.getBalance()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("출금 실패 - 잔액 부족")
    void withdraw_fail_insufficient_balance() {
        // given
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(10000L));
        request.setMemo("출금메모");

        Account account = Account.builder()
                .userId(1L)
                .userName("테스터")
                .accountNumber("1234567890")
                .balance(BigDecimal.valueOf(5000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(accountRepository.findByUserId(1L)).willReturn(Optional.of(account));

        // when & then
        assertThatThrownBy(() -> accountService.withdraw(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
    }

    @Test
    @DisplayName("계좌 조회 실패 - 계좌 없음")
    void getAccount_fail_not_found() {
        given(accountRepository.findByUserId(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.getAccount(1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("거래내역 조회 성공 - 비어있음")
    void getTransactions_success_empty() {
        Account account = Account.builder()
                .userId(1L)
                .userName("테스터")
                .accountNumber("1234567890")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 거래 내역 없는 케이스
        given(accountRepository.findByUserId(1L)).willReturn(Optional.of(account));
        // account.getTransactions()가 null 또는 비어있는 경우라면,
        // Account Entity에서 초기화(List.of() 또는 new ArrayList<>())가 되어있어야 함

        var result = accountService.getTransactions(1L);
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
