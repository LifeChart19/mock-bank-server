package com.example.mockbank.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    CREATE_USER_SUCCESS(HttpStatus.CREATED, "유저를 생성했습니다."),
    UPDATE_USER_SUCCESS(HttpStatus.OK, "유저를 수정했습니다."),
    DELETE_USER_SUCCESS(HttpStatus.OK, "유저탈퇴가 완료되었습니다."),
    GET_USER_INFO_SUCCESS(HttpStatus.OK, "내 정보를 조회했습니다."),
    GET_USER_PROFILE_SUCCESS(HttpStatus.OK, "유저 프로필을 조회했습니다."),
    GET_ACCOUNT_SUCCESS(HttpStatus.OK, "계좌 잔액 조회를 성공했습니다."),
    GET_TRANSACTIONS_SUCCESS(HttpStatus.OK, "거래 내역 조회를 성공했습니다."),
    GET_TRANSACTIONS_STATS_SUCCESS(HttpStatus.OK, "거래 내역 통계 조회를 성공했습니다."),
    DEPOSIT_SUCCESS(HttpStatus.OK, "입금이 완료되었습니다."),
    WITHDRAW_SUCCESS(HttpStatus.OK, "출금이 완료되었습니다.");

    private final HttpStatus status;
    private final String message;
}
