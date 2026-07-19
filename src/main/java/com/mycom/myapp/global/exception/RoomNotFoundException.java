package com.mycom.myapp.global.exception;

public class RoomNotFoundException extends RuntimeException {
	public RoomNotFoundException(String message) {
		super(message);
	}
}
