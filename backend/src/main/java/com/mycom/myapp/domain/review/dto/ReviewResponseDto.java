package com.mycom.myapp.domain.review.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.review.entity.Review;

public record ReviewResponseDto(Long id, Long memberId, Long roomId, Integer rating, String content, LocalDateTime createdAt) {
	public static ReviewResponseDto from(Review review) {
		return new ReviewResponseDto(review.getId(), review.getMember().getId(), review.getRoom().getId(), review.getRating(), review.getContent(), review.getCreatedAt());
	}
}
