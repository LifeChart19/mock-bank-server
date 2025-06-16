package com.example.mockbank.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountCreateRequest {
    private String accountNumber;
    private Long userId;
}
