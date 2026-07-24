package com.mycom.myapp.domain.room.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.room.entity.Room;

public record RoomResponseDto(Long id, String name, Integer capacity, Integer price, Integer discountedPrice, boolean wishlisted, Long wishlistCount, Double averageRating, Long reviewCount, LocalDateTime createdAt) {

	public static RoomResponseDto from(Room room, Integer discountedPrice) {
		return new RoomResponseDto(room.getId(), room.getName(), room.getCapacity(), room.getPrice(), discountedPrice, false, 0L, 0.0, 0L, room.getCreatedAt());
	}

	public static RoomResponseDto from(Room room, Integer discountedPrice, boolean wishlisted, Long wishlistCount, Double averageRating, Long reviewCount) {
		return new RoomResponseDto(room.getId(), room.getName(), room.getCapacity(), room.getPrice(), discountedPrice, wishlisted, wishlistCount, averageRating != null ? averageRating : 0.0, reviewCount, room.getCreatedAt());
	}
}
