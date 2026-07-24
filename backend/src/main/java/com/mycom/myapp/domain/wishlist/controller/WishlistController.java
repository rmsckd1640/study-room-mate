package com.mycom.myapp.domain.wishlist.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.wishlist.dto.WishlistCreateRequest;
import com.mycom.myapp.domain.wishlist.dto.WishlistResponseDto;
import com.mycom.myapp.domain.wishlist.service.WishlistService;
import com.mycom.myapp.global.common.dto.ResultDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlists")
public class WishlistController {

	private final WishlistService wishlistService;

	@Operation(description = "USER : 즐겨찾기 추가")
	@PostMapping
	public ResponseEntity<ResultDto<WishlistResponseDto>> create(@RequestBody @Valid WishlistCreateRequest request) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		WishlistResponseDto data = wishlistService.createWishlist(username, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResultDto.<WishlistResponseDto>builder().message("즐겨찾기 성공").data(data).build());
	}

	@Operation(description = "USER : 즐겨찾기 삭제")
	@DeleteMapping("/{id}")
	public ResponseEntity<ResultDto<Void>> delete(@PathVariable("id") Long id) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		wishlistService.deleteWishlist(username, id);
		return ResponseEntity.ok(ResultDto.<Void>builder().message("즐겨찾기 삭제 성공").data(null).build());
	}

	@Operation(description = "USER : 즐겨찾기 조회")
	@GetMapping
	public ResponseEntity<ResultDto<List<WishlistResponseDto>>> getWishlists() {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<WishlistResponseDto> data = wishlistService.getWishlistsByMember(username);
		return ResponseEntity.ok(ResultDto.<List<WishlistResponseDto>>builder().message("즐겨찾기 조회 성공").data(data).build());
	}

	@Operation(description = "USER : 특정 스터디룸의 즐겨찾기된 횟수 조회")
	@GetMapping("/room/{roomId}")
	public ResponseEntity<ResultDto<Long>> countByRoom(@PathVariable("roomId") Long roomId) {
		Long data = wishlistService.countByRoomId(roomId);
		return ResponseEntity.ok(ResultDto.<Long>builder().message("방의 즐겨찾기된 횟수 조회 성공").data(data).build());
	}
}
