package com.example.mockbank.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WithdrawRequest {
    @NotNull(message = "입금 금액은 필수입니다.")
    @Min(value = 1, message = "입금 금액은 1원 이상이어야 합니다.")
    private BigDecimal amount;
    private String memo; // 선택: 출금 메모
}
