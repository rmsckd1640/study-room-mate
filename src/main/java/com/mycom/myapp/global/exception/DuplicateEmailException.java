package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BaseException {
    public DuplicateEmailException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
