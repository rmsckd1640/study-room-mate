package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotAllowedException extends BaseException {
	public ReviewNotAllowedException(String message) {
		super(HttpStatus.FORBIDDEN, message);
	}

}
