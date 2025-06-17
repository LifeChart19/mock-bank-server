package com.example.mockbank.application.dto;

import lombok.Getter;

@Getter
public class AccountCreatedEvent {
    private Long userId;
    private String email;
    private String nickname;
    private String createdAt;
}

