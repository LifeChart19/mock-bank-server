package com.example.mockbank.domain.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Transaction {

    private String transactionId;

    private String accountNumber;

    private BigDecimal amount;

    private String type; // DEPOSIT or WITHDRAW

    private LocalDateTime transactedAt;
}
