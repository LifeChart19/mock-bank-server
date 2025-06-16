package com.example.mockbank.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private String type; // "DEPOSIT" or "WITHDRAWAL"
    private BigDecimal amount;
    private LocalDateTime time;
}
