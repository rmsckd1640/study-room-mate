package com.mycom.myapp.domain.room.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.room.entity.Room;

public record RoomDto(Long id, String name, String location, Integer capacity, Integer price, LocalDateTime createdAt) {
	public static RoomDto from(Room room) {
		return new RoomDto(room.getId(), room.getName(), room.getLocation(), room.getCapacity(), room.getPrice(), room.getCreatedAt());
	}
}
