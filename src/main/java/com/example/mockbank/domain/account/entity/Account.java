package com.example.mockbank.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public void deposit(Long amount) {
        this.balance = this.balance.add(BigDecimal.valueOf(amount));
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw(Long amount) {
        this.balance = this.balance.subtract(BigDecimal.valueOf(amount));
        this.updatedAt = LocalDateTime.now();
    }


}
