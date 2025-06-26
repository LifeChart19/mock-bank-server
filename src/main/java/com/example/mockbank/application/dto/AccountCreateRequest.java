package com.example.mockbank.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequest {
    private String accountNumber;
    private Long userId;
    private String userName;
}
