package com.example.mockbank.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber; // 고유 계좌 번호

    private Long userId;

    private String userName;

    private BigDecimal balance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
