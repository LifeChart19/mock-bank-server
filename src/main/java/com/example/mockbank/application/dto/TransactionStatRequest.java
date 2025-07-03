package com.example.mockbank.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatRequest {
    @NotNull(message = "시작 월은 필수입니다.")
    private YearMonth startYM; // ex: 2025-06

    @NotNull(message = "종료 월은 필수입니다.")
    private YearMonth endYM;   // ex: 2025-07
}