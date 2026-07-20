package com.mycom.myapp.domain.room.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.service.RoomService;
import com.mycom.myapp.global.common.dto.ResultDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	@GetMapping("/{id}")
	public ResponseEntity<ResultDto<RoomResponseDto>> getRoom(@PathVariable("id") Long id) {
		RoomResponseDto data = roomService.getRoom(id);
		return ResponseEntity.ok(ResultDto.<RoomResponseDto>builder().message("조회 성공").data(data).build());
	}

	@GetMapping
	public ResponseEntity<ResultDto<Page<RoomResponseDto>>> getRooms(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<RoomResponseDto> data = roomService.getRooms(pageable);
		return ResponseEntity.ok(ResultDto.<Page<RoomResponseDto>>builder().message("조회 성공").data(data).build());
	}

	@GetMapping("/search/name")
	public ResponseEntity<ResultDto<List<RoomResponseDto>>> searchByName(@RequestParam String name) {
		List<RoomResponseDto> data = roomService.searchByName(name);
		return ResponseEntity.ok(ResultDto.<List<RoomResponseDto>>builder().message("검색 성공").data(data).build());
	}

	@GetMapping("/search/location")
	public ResponseEntity<ResultDto<List<RoomResponseDto>>> searchByLocation(@RequestParam String location) {
		List<RoomResponseDto> data = roomService.searchByLocation(location);
		return ResponseEntity.ok(ResultDto.<List<RoomResponseDto>>builder().message("검색 성공").data(data).build());
	}

	@GetMapping("/search/capacity")
	public ResponseEntity<ResultDto<List<RoomResponseDto>>> searchByCapacity(@RequestParam Integer capacity) {
		List<RoomResponseDto> data = roomService.searchByMinCapacity(capacity);
		return ResponseEntity.ok(ResultDto.<List<RoomResponseDto>>builder().message("검색 성공").data(data).build());
	}

	@GetMapping("/search/price")
	public ResponseEntity<ResultDto<List<RoomResponseDto>>> searchByPrice(@RequestParam Integer price) {
		List<RoomResponseDto> data = roomService.searchByMaxPrice(price);
		return ResponseEntity.ok(ResultDto.<List<RoomResponseDto>>builder().message("검색 성공").data(data).build());
	}

	@PostMapping
	public ResponseEntity<ResultDto<RoomResponseDto>> create(@RequestBody @Valid RoomCreateRequest request) {
		RoomResponseDto data = roomService.createRoom(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResultDto.<RoomResponseDto>builder().message("생성 성공").data(data).build());
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ResultDto<RoomResponseDto>> update(@PathVariable("id") Long id, @RequestBody @Valid RoomUpdateRequest request) {
		RoomResponseDto data = roomService.updateRoom(id, request);
		return ResponseEntity.ok(ResultDto.<RoomResponseDto>builder().message("수정 성공").data(data).build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResultDto<Void>> delete(@PathVariable("id") Long id) {
		roomService.deleteRoom(id);
		return ResponseEntity.ok(ResultDto.<Void>builder().message("삭제 성공").data(null).build());
	}
}
