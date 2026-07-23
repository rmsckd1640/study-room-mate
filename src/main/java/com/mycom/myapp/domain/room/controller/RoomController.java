package com.mycom.myapp.domain.room.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	@Operation(description = "USER : 특정 스터디룸 조회")
	@GetMapping("/{id}")
	public ResponseEntity<ResultDto<RoomResponseDto>> getRoom(@PathVariable("id") Long id) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		RoomResponseDto data = roomService.getRoom(username, id);
		return ResponseEntity.ok(ResultDto.<RoomResponseDto>builder().message("조회 성공").data(data).build());
	}

	@Operation(description = "USER : 스터디룸 페이징 조회")
	@GetMapping
	public ResponseEntity<ResultDto<Page<RoomResponseDto>>> getRooms(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Page<RoomResponseDto> data = roomService.getRooms(username, pageable);
		return ResponseEntity.ok(ResultDto.<Page<RoomResponseDto>>builder().message("조회 성공").data(data).build());
	}

	@Operation(description = "USER : 스터디룸 이름, 특정 수용인원 이상, 특정 가격으로 스터디룸 검색")
	@GetMapping("/search")
	public ResponseEntity<ResultDto<List<RoomResponseDto>>> search(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "capacity", required = false) Integer capacity, @RequestParam(value = "price", required = false) Integer price) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<RoomResponseDto> data = roomService.search(username, name, capacity, price);
		return ResponseEntity.ok(ResultDto.<List<RoomResponseDto>>builder().message("검색 성공").data(data).build());
	}

	@Operation(description = "USER : 페이징을 이용하여 스터디룸 이름, 특정 수용인원 이상, 특정 가격으로 스터디룸 검색")
	@GetMapping("/search/page")
	public ResponseEntity<ResultDto<Page<RoomResponseDto>>> searchWithPaging(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "capacity", required = false) Integer capacity, @RequestParam(value = "price", required = false) Integer price, @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Page<RoomResponseDto> data = roomService.searchWithPaging(username, name, capacity, price, pageable);
		return ResponseEntity.ok(ResultDto.<Page<RoomResponseDto>>builder().message("검색 성공").data(data).build());
	}

	@Operation(description = "ADMIN : 스터디룸 생성")
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<ResultDto<RoomResponseDto>> create(@RequestBody @Valid RoomCreateRequest request) {
		RoomResponseDto data = roomService.createRoom(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResultDto.<RoomResponseDto>builder().message("생성 성공").data(data).build());
	}

	@Operation(description = "ADMIN : 스터디룸 수정")
	@PatchMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<ResultDto<RoomResponseDto>> update(@PathVariable("id") Long id, @RequestBody @Valid RoomUpdateRequest request) {
		RoomResponseDto data = roomService.updateRoom(id, request);
		return ResponseEntity.ok(ResultDto.<RoomResponseDto>builder().message("수정 성공").data(data).build());
	}

	@Operation(description = "ADMIN : 스터디룸 삭제")
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<ResultDto<Void>> delete(@PathVariable("id") Long id) {
		roomService.deleteRoom(id);
		return ResponseEntity.ok(ResultDto.<Void>builder().message("삭제 성공").data(null).build());
	}
}
