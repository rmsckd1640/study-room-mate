package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class ReviewAccessDeniedException extends BaseException {

	public ReviewAccessDeniedException(String message) {
		super(HttpStatus.FORBIDDEN, message);
	}
}
