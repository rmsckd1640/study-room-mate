package com.mycom.myapp.domain.room.dto;

import lombok.Data;

@Data
public class RoomResultDto {

	private String result;
	private int status;
	private String Message;
	private RoomDto data;
}
