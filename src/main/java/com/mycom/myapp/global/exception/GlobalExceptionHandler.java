package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mycom.myapp.global.common.dto.ResultDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ResultDto<Void>> handleBaseException(BaseException e) {
		log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
		return ResponseEntity.status(e.getStatus()).body(ResultDto.<Void>builder().message(e.getMessage()).data(null).build());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResultDto<Void>> handleValidation(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		String message = fieldError != null ? fieldError.getDefaultMessage() : "잘못된 입력값입니다.";

		log.warn("Validation failed: {}", message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultDto.<Void>builder().message(message).data(null).build());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ResultDto<Void>> handleIllegalArgument(IllegalArgumentException e) {
		// Room.update(), Room.changePrice() 내부에서 던지는 검증 예외
		log.warn("IllegalArgumentException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultDto.<Void>builder().message(e.getMessage()).data(null).build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResultDto<Void>> handleException(Exception e) {
		log.error("Unexpected exception", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResultDto.<Void>builder().message("서버 내부 오류가 발생했습니다.").data(null).build());
	}
}