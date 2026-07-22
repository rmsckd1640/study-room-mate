package com.mycom.myapp.domain.review.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.review.dto.ReviewCreateRequest;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewUpdateRequest;
import com.mycom.myapp.domain.review.dto.RoomRatingSummaryDto;

public interface ReviewService {

	ReviewResponseDto createReview(String username, ReviewCreateRequest request);

	ReviewResponseDto updateReview(String username, Long reviewId, ReviewUpdateRequest request);

	void deleteReview(String username, Long reviewId);

	List<ReviewResponseDto> getReviewsByMember(Long memberId);

	Page<ReviewResponseDto> getReviewsByRoom(Long roomId, Pageable pageable);

	RoomRatingSummaryDto getRatingSummary(Long roomId);
}
