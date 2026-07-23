package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends BaseException {
	public ReservationNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}
}
