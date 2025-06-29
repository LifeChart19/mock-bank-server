package com.example.mockbank.common.response;

import com.example.mockbank.common.enums.ErrorCode;
import com.example.mockbank.common.enums.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> onSuccess(SuccessCode code, T data) {
        return new ApiResponse<>(true, code.name(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> onError(ErrorCode code) {
        return new ApiResponse<>(false, code.name(), code.getMessage(), null);
    }
}
