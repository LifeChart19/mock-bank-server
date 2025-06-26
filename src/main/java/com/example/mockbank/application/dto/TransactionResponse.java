package com.example.mockbank.application.dto;

import com.example.mockbank.domain.account.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String type;
    private LocalDateTime createdAt;
    private String description;
    private String memo;

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType().name(),
                t.getCreatedAt(),
                t.getDescription(),
                t.getMemo()
        );
    }
}



