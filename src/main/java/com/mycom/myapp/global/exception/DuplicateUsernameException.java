package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUsernameException extends BaseException {
    public DuplicateUsernameException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
