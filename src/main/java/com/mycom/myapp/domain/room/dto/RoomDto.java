package com.mycom.myapp.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
	private Long id;
	private String name;
	private String location;
	private int capacity;
	private int price;
}
