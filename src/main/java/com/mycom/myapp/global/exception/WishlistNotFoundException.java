package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

public class WishlistNotFoundException extends BaseException {
	public WishlistNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}
}
