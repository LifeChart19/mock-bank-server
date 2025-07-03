package com.example.mockbank.adapter.in.web;

import com.example.mockbank.application.dto.*;
import com.example.mockbank.application.service.AccountService;
import com.example.mockbank.common.enums.ErrorCode;
import com.example.mockbank.common.enums.SuccessCode;
import com.example.mockbank.common.exception.CustomException;
import com.example.mockbank.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return ResponseEntity
                .status(SuccessCode.CREATE_USER_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(SuccessCode.CREATE_USER_SUCCESS, accountService.createAccount(request)));
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(
            @PathVariable Long userId,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity
                .status(SuccessCode.DEPOSIT_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(SuccessCode.DEPOSIT_SUCCESS, accountService.deposit(userId, request)));
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(
            @PathVariable Long userId,
            @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity
                .status(SuccessCode.WITHDRAW_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(SuccessCode.WITHDRAW_SUCCESS, accountService.withdraw(userId, request)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long userId) {
        return ResponseEntity
                .status(SuccessCode.GET_ACCOUNT_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(SuccessCode.GET_ACCOUNT_SUCCESS, accountService.getAccount(userId)));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity
                .status(SuccessCode.GET_TRANSACTIONS_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(SuccessCode.GET_TRANSACTIONS_SUCCESS, accountService.getTransactions(userId)));
    }

    @PostMapping("/{userId}/transactions/stats")
    public ApiResponse<TransactionStatResponse> getTransactionStats(
            @PathVariable Long userId,
            @RequestBody TransactionStatRequest request
    ) {
        if (request.getStartYM().isAfter(request.getEndYM())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
        TransactionStatResponse resp = accountService.getTransactionStats(
                userId,
                request.getStartYM(),
                request.getEndYM()
        );
        return ApiResponse.onSuccess(SuccessCode.GET_TRANSACTIONS_STATS_SUCCESS, resp);
    }
}
