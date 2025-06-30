package com.example.mockbank.domain.account.repository;

import com.example.mockbank.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    boolean existsByAccountNumber(String accountNumber);
}
