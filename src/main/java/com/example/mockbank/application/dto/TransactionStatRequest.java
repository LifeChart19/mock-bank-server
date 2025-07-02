package com.example.mockbank.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatRequest {
    private YearMonth startYM; // ex: 2025-06
    private YearMonth endYM;   // ex: 2025-07
}