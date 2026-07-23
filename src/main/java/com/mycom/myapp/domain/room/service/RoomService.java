package com.mycom.myapp.domain.room.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;

public interface RoomService {
	RoomResponseDto getRoom(String username, Long roomId);

	Page<RoomResponseDto> getRooms(String username, Pageable pageable);

	List<RoomResponseDto> search(String username, String name, Integer capacity, Integer price);

	Page<RoomResponseDto> searchWithPaging(String username, String name, Integer capacity, Integer price, Pageable pageable);

	RoomResponseDto createRoom(RoomCreateRequest request);

	RoomResponseDto updateRoom(Long roomId, RoomUpdateRequest request);

	void deleteRoom(Long roomId);
}
