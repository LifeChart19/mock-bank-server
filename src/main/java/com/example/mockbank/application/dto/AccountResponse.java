package com.example.mockbank.application.dto;

import com.example.mockbank.domain.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AccountResponse {
    private String accountNumber;
    private Long userId;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getUserId(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }
}
