package com.example.mockbank.application.dto;

import com.example.mockbank.domain.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountResponse {
    private Long accountId;
    private Long userId;
    private String accountNumber;
    private BigDecimal balance;

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getBalance()
        );
    }
}

