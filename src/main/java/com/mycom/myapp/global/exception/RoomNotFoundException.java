package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class RoomNotFoundException extends BaseException {
    public RoomNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
