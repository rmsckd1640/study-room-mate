package com.mycom.myapp.domain.room.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.exception.RoomNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	@Override
	public RoomResponseDto getRoom(Long roomId) {
		Room room = findRoomOrThrow(roomId);
		return RoomResponseDto.from(room);
	}

	@Override
	public Page<RoomResponseDto> getRooms(Pageable pageable) {
		return roomRepository.findAll(pageable).map(RoomResponseDto::from);
	}

	@Override
	public List<RoomResponseDto> searchByName(String name) {
		return roomRepository.findByNameContaining(name).stream().map(RoomResponseDto::from).toList();
	}

	@Override
	public List<RoomResponseDto> searchByLocation(String location) {
		return roomRepository.findByLocationContaining(location).stream().map(RoomResponseDto::from).toList();
	}

	@Override
	@Transactional
	public RoomResponseDto createRoom(RoomCreateRequest request) {
		Room room = request.toEntity();
		Room saved = roomRepository.save(room);
		return RoomResponseDto.from(saved);
	}

	@Override
	@Transactional
	public RoomResponseDto updateRoom(Long roomId, RoomUpdateRequest request) {
		Room room = findRoomOrThrow(roomId);
		room.update(request.name(), request.location(), request.capacity(), request.price());
		return RoomResponseDto.from(room);
	}

	@Override
	@Transactional
	public void deleteRoom(Long roomId) {
		Room room = findRoomOrThrow(roomId);
		roomRepository.delete(room);
	}

	private Room findRoomOrThrow(Long roomId) {
		return roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("존재하지 않는 room입니다. id=" + roomId));
	}
}
