package com.example.mockbank.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreatedEvent {
    private Long userId;
    private String email;
    private String nickname;
    private String userName;
    private LocalDateTime createdAt;
}
