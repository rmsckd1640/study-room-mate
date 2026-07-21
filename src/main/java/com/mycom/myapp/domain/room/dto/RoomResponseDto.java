package com.mycom.myapp.domain.room.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.room.entity.Room;

public record RoomResponseDto(Long id, String name, Integer capacity, Integer price, LocalDateTime createdAt) {
	public static RoomResponseDto from(Room room) {
		return new RoomResponseDto(room.getId(), room.getName(), room.getCapacity(), room.getPrice(), room.getCreatedAt());
	}
}
