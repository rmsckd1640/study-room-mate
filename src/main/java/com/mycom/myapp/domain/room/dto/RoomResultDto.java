package com.mycom.myapp.domain.room.dto;

import java.util.List;

import com.mycom.myapp.domain.room.entity.Room;

import lombok.Data;

@Data
public class RoomResultDto {

	private String result;
	private Room data;
	private List<Room> list;
}
