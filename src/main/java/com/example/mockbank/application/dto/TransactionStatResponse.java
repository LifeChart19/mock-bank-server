package com.example.mockbank.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal avgMonthlyIncome;
    private BigDecimal avgMonthlyExpense;
}
