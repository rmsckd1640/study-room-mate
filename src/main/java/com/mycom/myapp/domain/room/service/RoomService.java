package com.mycom.myapp.domain.room.service;

import java.util.List;

import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.domain.room.dto.RoomResultDto;

public interface RoomService {

	RoomResultDto insertRoom(RoomDto roomDto);
	RoomResultDto updateRoom(RoomDto roomDto);
	RoomResultDto deleteRoom(Long id);
	List<RoomDto> findAll();
	RoomDto findById(Long id);
	RoomDto findByName(String name);
	List<RoomDto> findByLocation(String location);
	List<RoomDto> findByPrice(int price);

}
