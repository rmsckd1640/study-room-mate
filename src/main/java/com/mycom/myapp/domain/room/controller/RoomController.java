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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	@GetMapping("/{id}")
	public ResponseEntity<RoomResponseDto> getRoom(@PathVariable Long id) {
		return ResponseEntity.ok(roomService.getRoom(id));
	}

	@GetMapping
	public ResponseEntity<Page<RoomResponseDto>> getRooms(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(roomService.getRooms(pageable));
	}

	@GetMapping("/search/name")
	public ResponseEntity<List<RoomResponseDto>> searchByName(@RequestParam String name) {
		return ResponseEntity.ok(roomService.searchByName(name));
	}

	@GetMapping("/search/location")
	public ResponseEntity<List<RoomResponseDto>> searchByLocation(@RequestParam String location) {
		return ResponseEntity.ok(roomService.searchByLocation(location));
	}

	@GetMapping("/search/capacity")
	public ResponseEntity<List<RoomResponseDto>> searchByCapacity(@RequestParam Integer capacity) {
		return ResponseEntity.ok(roomService.searchByMinCapacity(capacity));
	}

	@GetMapping("/search/price")
	public ResponseEntity<List<RoomResponseDto>> searchByPrice(@RequestParam Integer price) {
		return ResponseEntity.ok(roomService.searchByMaxPrice(price));
	}

	@PostMapping
	public ResponseEntity<RoomResponseDto> create(@RequestBody @Valid RoomCreateRequest request) {
		RoomResponseDto result = roomService.createRoom(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<RoomResponseDto> update(@PathVariable Long id, @RequestBody @Valid RoomUpdateRequest request) {
		return ResponseEntity.ok(roomService.updateRoom(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		roomService.deleteRoom(id);
		return ResponseEntity.noContent().build();
	}

}
