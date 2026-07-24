package com.mycom.myapp.domain.review.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.review.dto.ReviewCreateRequest;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewUpdateRequest;
import com.mycom.myapp.domain.review.dto.RoomRatingSummaryDto;
import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.exception.DuplicateReviewException;
import com.mycom.myapp.global.exception.ReviewAccessDeniedException;
import com.mycom.myapp.global.exception.ReviewNotAllowedException;
import com.mycom.myapp.global.exception.ReviewNotFoundException;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	private final ReservationRepository reservationRepository;

	private Review findReviewOrThrow(Long reviewId) {
		return reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException("존재하지 않는 리뷰입니다."));
	}

	private Member findMemberOrThrow(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
	}

	private Room findRoomOrThrow(Long roomId) {
		return roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("존재하지 않는 room입니다."));
	}

	private void validateHasConfirmedReservation(Long memberId, Long roomId) {
		boolean hasConfirmedReservation = reservationRepository.existsByMember_IdAndRoom_IdAndStatus(memberId, roomId, ReservationStatus.CONFIRMED);
		if (!hasConfirmedReservation) {
			throw new ReviewNotAllowedException("확정된 예약이 있어야 리뷰를 작성할 수 있습니다.");
		}
	}

	private void validateNotDuplicate(Long memberId, Long roomId) {
		if (reviewRepository.existsByMember_IdAndRoom_Id(memberId, roomId)) {
			throw new DuplicateReviewException("이미 이 방에 대한 리뷰를 작성했습니다.");
		}
	}

	private void validateOwner(Review review, Long memberId) {
		if (!review.getMember().getId().equals(memberId)) {
			throw new ReviewAccessDeniedException("본인이 작성한 리뷰만 수정/삭제할 수 있습니다.");
		}
	}

	@Override
	@Transactional
	public ReviewResponseDto createReview(String username, ReviewCreateRequest request) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		Room room = findRoomOrThrow(request.roomId());
		validateHasConfirmedReservation(member.getId(), request.roomId());
		validateNotDuplicate(member.getId(), request.roomId());
		Review review = Review.builder().member(member).room(room).rating(request.rating()).content(request.content()).build();
		Review saved = reviewRepository.save(review);
		return ReviewResponseDto.from(saved);
	}

	@Override
	@Transactional
	public ReviewResponseDto updateReview(String username, Long reviewId, ReviewUpdateRequest request) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		Review review = findReviewOrThrow(reviewId);
		validateOwner(review, member.getId());
		review.update(request.rating(), request.content());
		return ReviewResponseDto.from(review);
	}

	@Override
	@Transactional
	public void deleteReview(String username, Long reviewId) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		Review review = findReviewOrThrow(reviewId);
		validateOwner(review, member.getId());
		reviewRepository.delete(review);
	}

	@Override
	public List<ReviewResponseDto> getReviewsByMember(Long memberId) {
		return reviewRepository.findByMemberId(memberId).stream().map(ReviewResponseDto::from).toList();
	}

	@Override
	public Page<ReviewResponseDto> getReviewsByRoom(Long roomId, Pageable pageable) {
		return reviewRepository.findByRoomIdOrderByRatingDesc(roomId, pageable).map(ReviewResponseDto::from);
	}

	@Override
	public RoomRatingSummaryDto getRatingSummary(Long roomId) {
		Double average = reviewRepository.findAverageRatingByRoomId(roomId);
		long count = reviewRepository.countByRoomId(roomId);
		return new RoomRatingSummaryDto(average != null ? average : 0.0, count);
	}
}
