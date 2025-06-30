package com.example.mockbank.domain.account.repository;

import com.example.mockbank.domain.account.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
