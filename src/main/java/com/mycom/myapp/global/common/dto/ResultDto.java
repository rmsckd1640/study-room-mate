package com.mycom.myapp.global.common.dto;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDto<T> {
	
	private String result;
	private HttpStatus status;
	private String message;
	private T data;
	
}
