package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class WithdrawnMemberException extends BaseException {
    public WithdrawnMemberException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
