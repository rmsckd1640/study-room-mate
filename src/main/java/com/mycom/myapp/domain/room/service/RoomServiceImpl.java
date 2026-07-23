package com.mycom.myapp.domain.room.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;

	private final MemberRepository memberRepository;

	private MemberGrade getMemberGrade(String username) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		return member.getGrade();
	}

	@Override
	public RoomResponseDto getRoom(String username, Long roomId) {
		Room room = findRoomOrThrow(roomId);
		MemberGrade grade = getMemberGrade(username);
		return RoomResponseDto.from(room, grade.applyDiscount(room.getPrice()));
	}

	@Override
	public Page<RoomResponseDto> getRooms(String username, Pageable pageable) {
		MemberGrade grade = getMemberGrade(username);
		return roomRepository.findAll(pageable).map(room -> RoomResponseDto.from(room, grade.applyDiscount(room.getPrice())));
	}

	@Override
	public List<RoomResponseDto> search(String username, String name, Integer capacity, Integer price) {
		MemberGrade grade = getMemberGrade(username);
		return roomRepository.search(name, capacity, price).stream().map(room -> RoomResponseDto.from(room, grade.applyDiscount(room.getPrice()))).toList();
	}

	@Override
	public Page<RoomResponseDto> searchWithPaging(String username, String name, Integer capacity, Integer price, Pageable pageable) {
		MemberGrade grade = getMemberGrade(username);
		return roomRepository.search(name, capacity, price, pageable).map(room -> RoomResponseDto.from(room, grade.applyDiscount(room.getPrice())));
	}

	@Override
	@Transactional
	public RoomResponseDto createRoom(RoomCreateRequest request) {
		Room room = request.toEntity();
		Room saved = roomRepository.save(room);
		return RoomResponseDto.from(saved, null);
	}

	@Override
	@Transactional
	public RoomResponseDto updateRoom(Long roomId, RoomUpdateRequest request) {
		Room room = findRoomOrThrow(roomId);
		room.update(request.name(), request.capacity(), request.price());
		return RoomResponseDto.from(room, null);
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
