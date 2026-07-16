package com.mycom.myapp.domain.room.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.domain.room.dto.RoomResultDto;
import com.mycom.myapp.domain.room.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	@Override
	public RoomResultDto insertRoom(RoomDto roomDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoomResultDto updateRoom(RoomDto roomDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoomResultDto deleteRoom(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RoomDto> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoomDto findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoomDto findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RoomDto> findByLocation(String location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RoomDto> findByPrice(int price) {
		// TODO Auto-generated method stub
		return null;
	}

}
