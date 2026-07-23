package com.mycom.myapp.domain.room.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.room.entity.Room;

public record RoomResponseDto(Long id, String name, Integer capacity, Integer price, Integer discountedPrice, LocalDateTime createdAt) {
	public static RoomResponseDto from(Room room, Integer discountedPrice) {
		return new RoomResponseDto(room.getId(), room.getName(), room.getCapacity(), room.getPrice(), discountedPrice, room.getCreatedAt());
	}
}
