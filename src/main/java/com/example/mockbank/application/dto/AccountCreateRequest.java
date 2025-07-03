package com.example.mockbank.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequest {
    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNumber;

    @NotNull(message = "유저ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "유저명은 필수입니다.")
    private String userName;

    private BigDecimal salary;
}
