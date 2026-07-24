package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BaseException {
	public PaymentNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}
}
