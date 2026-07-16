package com.mycom.myapp.domain.room.service;

import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.domain.room.dto.RoomResultDto;

public interface RoomService {

	RoomResultDto insertRoom(RoomDto roomDto);
	RoomResultDto updateRoom(RoomDto roomDto);
	RoomResultDto deleteRoom(Long id);
	RoomResultDto findAll();
	RoomResultDto findById(Long id);
	RoomResultDto findByName(String name);
	RoomResultDto findByLocation(String location);
	RoomResultDto findByCapacityGreaterThanEqual(int capacity);
	RoomResultDto findByPriceGreaterThanEqual(int price);
}
