package com.example.mockbank.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BalanceResponse {
    private String accountNumber;
    private BigDecimal balance;
}
