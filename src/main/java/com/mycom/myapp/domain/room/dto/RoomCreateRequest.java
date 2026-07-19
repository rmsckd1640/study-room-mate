package com.mycom.myapp.domain.room.dto;

import com.mycom.myapp.domain.room.entity.Room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(@NotBlank(message = "이름은 필수입니다.") String name,

		@NotBlank(message = "지역은 필수입니다.") String location,

		@NotNull @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.") Integer capacity,

		@NotNull @Min(value = 0, message = "가격은 0 이상이어야 합니다.") Integer price) {
	public Room toEntity() {
		return Room.builder().name(name).location(location).capacity(capacity).price(price).build();
	}
}
