package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateReviewException extends BaseException {
	public DuplicateReviewException(String message) {
		super(HttpStatus.CONFLICT, message);
	}

}
