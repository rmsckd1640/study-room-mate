package com.mycom.myapp.global.common.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDto<T> {
	
	private int status;
	private String result;
	private List<T> listDto;
	private Map<?, T> mapDto;
	
}
