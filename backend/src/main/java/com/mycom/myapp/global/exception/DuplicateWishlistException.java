package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateWishlistException extends BaseException {
	public DuplicateWishlistException(String message) {
		super(HttpStatus.CONFLICT, message);
	}
}
