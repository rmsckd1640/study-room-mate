package com.mycom.myapp.domain.room.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;

public interface RoomService {
	RoomResponseDto getRoom(Long roomId);

	Page<RoomResponseDto> getRooms(Pageable pageable);

	List<RoomResponseDto> searchByName(String name);

	List<RoomResponseDto> searchByLocation(String location);

	List<RoomResponseDto> searchByMinCapacity(Integer capacity); // 추가

	List<RoomResponseDto> searchByMaxPrice(Integer price); // 추가

	RoomResponseDto createRoom(RoomCreateRequest request);

	RoomResponseDto updateRoom(Long roomId, RoomUpdateRequest request);

	void deleteRoom(Long roomId);
}
