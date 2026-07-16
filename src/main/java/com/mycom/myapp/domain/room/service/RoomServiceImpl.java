package com.mycom.myapp.domain.room.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.domain.room.dto.RoomResultDto;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	@Override
	public RoomResultDto insertRoom(RoomDto roomDto) {
		RoomResultDto roomResultDto = new RoomResultDto();
		try {
			Room room = Room
					.builder().name(roomDto.getName())//
					.location(roomDto.getLocation())//
					.capacity(roomDto.getCapacity())//
					.price(roomDto.getPrice())//
					.createdAt(LocalDateTime.now())//
					.updatedAt(null)//
					.deletedAt(null)//
					.build();
			roomRepository.save(room);
			roomResultDto.setResult("success");
		}
		catch (Exception e) {
			e.printStackTrace();
			roomResultDto.setResult("fail");
		}
		return roomResultDto;
	}

	@Override
	@Transactional
	public RoomResultDto updateRoom(RoomDto roomDto) {
		RoomResultDto roomResultDto = new RoomResultDto();
		Room room = roomRepository.findById(roomDto.getId()).orElse(null);
		if (room == null) {
			roomResultDto.setResult("fail");
			return roomResultDto;
		}
		room.setName(roomDto.getName());
		room.setLocation(roomDto.getLocation());
		room.setCapacity(roomDto.getCapacity());
		room.setPrice(roomDto.getPrice());
		room.setUpdatedAt(LocalDateTime.now());
		roomResultDto.setResult("success");
		return roomResultDto;
	}

	@Override
	public RoomResultDto deleteRoom(Long id) {
		RoomResultDto roomResultDto = new RoomResultDto();
		Room room = roomRepository.findById(id).orElse(null);
		if (room == null) {
			roomResultDto.setResult("fail");
			return roomResultDto;
		}
		try {
			roomRepository.delete(room);
			roomResultDto.setResult("success");
		}
		catch (Exception e) {
			e.printStackTrace();
			roomResultDto.setResult("fail");
		}
		return roomResultDto;
	}

	@Override
	public RoomResultDto findAll() {
		RoomResultDto roomResultDto = new RoomResultDto();
		roomResultDto.setList(roomRepository.findAll());
		roomResultDto.setResult("success");
		return roomResultDto;
	}

	@Override
	public RoomResultDto findById(Long id) {
		RoomResultDto roomResultDto = new RoomResultDto();
		Room room = roomRepository.findById(id).orElse(null);
		if (room == null) {
			roomResultDto.setResult("fail");
			return roomResultDto;
		}
		roomResultDto.setData(room);
		roomResultDto.setResult("success");
		return roomResultDto;

	}

	@Override
	public RoomResultDto findByName(String name) {
		RoomResultDto roomResultDto = new RoomResultDto();
		roomResultDto.setList(roomRepository.findByName(name));
		roomResultDto.setResult("success");
		return roomResultDto;
	}

	@Override
	public RoomResultDto findByLocation(String location) {
		RoomResultDto roomResultDto = new RoomResultDto();
		roomResultDto.setList(roomRepository.findByLocation(location));
		roomResultDto.setResult("success");
		return roomResultDto;
	}

	@Override
	public RoomResultDto findByCapacityGreaterThanEqual(int capacity) {
		RoomResultDto roomResultDto = new RoomResultDto();
		roomResultDto.setList(roomRepository.findByCapacityGreaterThanEqual(capacity));
		roomResultDto.setResult("success");
		return roomResultDto;
	}

	@Override
	public RoomResultDto findByPriceLessThanEqual(int price) {
		RoomResultDto roomResultDto = new RoomResultDto();
		roomResultDto.setList(roomRepository.findByPriceLessThanEqual(price));
		roomResultDto.setResult("success");
		return roomResultDto;
	}
}
