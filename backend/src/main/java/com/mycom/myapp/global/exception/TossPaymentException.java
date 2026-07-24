package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public class TossPaymentException extends BaseException {
	
	private final HttpStatusCode tossErrorCode;
	
	public TossPaymentException(String message, HttpStatusCode tossErrorCode, HttpStatus status) {
		super(status, message);
		this.tossErrorCode = tossErrorCode;
	}
	
}
