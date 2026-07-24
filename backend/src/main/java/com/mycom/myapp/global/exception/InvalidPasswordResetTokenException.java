package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

// 비밀번호 재설정 토큰이 존재하지 않거나 만료된 경우
public class InvalidPasswordResetTokenException extends BaseException {
    public InvalidPasswordResetTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
