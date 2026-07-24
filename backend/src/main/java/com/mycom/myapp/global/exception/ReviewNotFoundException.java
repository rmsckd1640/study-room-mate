package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends BaseException {
	public ReviewNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}

}
