package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateReservationException extends BaseException {
    public DuplicateReservationException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
