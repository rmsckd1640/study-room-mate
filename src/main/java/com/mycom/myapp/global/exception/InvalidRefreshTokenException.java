package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BaseException {
    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
