package com.example.mockbank.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountCreateRequest {
    private Long userId;
    private String userName;
}
