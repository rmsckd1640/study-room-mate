package com.mycom.myapp.domain.review.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.review.dto.ReviewCreateRequest;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewUpdateRequest;
import com.mycom.myapp.domain.review.dto.RoomRatingSummaryDto;
import com.mycom.myapp.domain.review.service.ReviewService;
import com.mycom.myapp.global.common.dto.ResultDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(description = "USER : 리뷰 생성")
	@PostMapping
	public ResponseEntity<ResultDto<ReviewResponseDto>> create(@RequestBody @Valid ReviewCreateRequest request) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ReviewResponseDto data = reviewService.createReview(username, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ResultDto.<ReviewResponseDto>builder().message("리뷰 생성 성공").data(data).build());
	}

	@Operation(description = "USER : 리뷰 수정")
	@PatchMapping("/{id}")
	public ResponseEntity<ResultDto<ReviewResponseDto>> update(@PathVariable("id") Long id, @RequestBody @Valid ReviewUpdateRequest request) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ReviewResponseDto data = reviewService.updateReview(username, id, request);
		return ResponseEntity.ok(ResultDto.<ReviewResponseDto>builder().message("리뷰 수정 성공").data(data).build());
	}

	@Operation(description = "USER : 리뷰 삭제")
	@DeleteMapping("/{id}")
	public ResponseEntity<ResultDto<Void>> delete(@PathVariable("id") Long id) {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		reviewService.deleteReview(username, id);
		return ResponseEntity.ok(ResultDto.<Void>builder().message("리뷰 삭제 성공").data(null).build());
	}

	@Operation(description = "USER : 본인 리뷰 조회")
	@GetMapping("/member/{memberId}")
	public ResponseEntity<ResultDto<List<ReviewResponseDto>>> getByMember(@PathVariable("memberId") Long memberId) {
		List<ReviewResponseDto> data = reviewService.getReviewsByMember(memberId);
		return ResponseEntity.ok(ResultDto.<List<ReviewResponseDto>>builder().message("조회 성공").data(data).build());
	}

	@Operation(description = "USER : 특정 스터디룸의 리뷰 조회")
	@GetMapping("/room/{roomId}")
	public ResponseEntity<ResultDto<Page<ReviewResponseDto>>> getByRoom(@PathVariable("roomId") Long roomId, @PageableDefault(size = 10, sort = "rating", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<ReviewResponseDto> data = reviewService.getReviewsByRoom(roomId, pageable);
		return ResponseEntity.ok(ResultDto.<Page<ReviewResponseDto>>builder().message("조회 성공").data(data).build());
	}

	@Operation(description = "USER : 특정 스터디룸의 평균 평점 조회")
	@GetMapping("/room/{roomId}/rating-summary")
	public ResponseEntity<ResultDto<RoomRatingSummaryDto>> getRatingSummary(@PathVariable("roomId") Long roomId) {
		RoomRatingSummaryDto data = reviewService.getRatingSummary(roomId);
		return ResponseEntity.ok(ResultDto.<RoomRatingSummaryDto>builder().message("조회 성공").data(data).build());
	}
}
