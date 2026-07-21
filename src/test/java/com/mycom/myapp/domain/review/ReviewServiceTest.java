package com.mycom.myapp.domain.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.review.dto.ReviewCreateRequest;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewUpdateRequest;
import com.mycom.myapp.domain.review.dto.RoomRatingSummaryDto;
import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.review.service.ReviewServiceImpl;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.exception.DuplicateReviewException;
import com.mycom.myapp.global.exception.ReviewAccessDeniedException;
import com.mycom.myapp.global.exception.ReviewNotAllowedException;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@InjectMocks
	private ReviewServiceImpl reviewService;

	private Member member;
	private Room room;

	@BeforeEach
	void setUp() {
		member = Member.builder().username("user1").password("password1").email("user1@test.com").name("회원1").build();
		ReflectionTestUtils.setField(member, "id", 1L);

		room = Room.builder().name("한강뷰 스튜디오").location("서울 성동구").capacity(4).price(150000).build();
		ReflectionTestUtils.setField(room, "id", 5L);
	}

	private Review createReview(Long id, Member member, Room room, Integer rating, String content) {
		Review review = Review.builder().member(member).room(room).rating(rating).content(content).build();
		ReflectionTestUtils.setField(review, "id", id);
		return review;
	}

	@Test
	@DisplayName("확정된 예약이 있고 중복 리뷰가 없으면 리뷰가 생성된다")
	void createReview_성공() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");
		Review savedReview = createReview(10L, member, room, 5, "최고예요");

		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(reservationRepository.existsByMember_IdAndRoom_IdAndStatus(1L, 5L, ReservationStatus.CONFIRMED)).willReturn(true);
		given(reviewRepository.existsByMember_IdAndRoom_Id(1L, 5L)).willReturn(false);
		given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

		// when
		ReviewResponseDto result = reviewService.createReview("user1", request);

		// then
		assertThat(result.rating()).isEqualTo(5);
		assertThat(result.content()).isEqualTo("최고예요");
		verify(reviewRepository, times(1)).save(any(Review.class));
	}

	@Test
	@DisplayName("확정된 예약이 없으면 ReviewNotAllowedException이 발생한다")
	void createReview_실패_예약없음() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");

		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(reservationRepository.existsByMember_IdAndRoom_IdAndStatus(1L, 5L, ReservationStatus.CONFIRMED)).willReturn(false);

		// when & then
		assertThrows(ReviewNotAllowedException.class, () -> reviewService.createReview("user1", request));
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("이미 활성 리뷰가 있으면 DuplicateReviewException이 발생한다")
	void createReview_실패_중복리뷰() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");

		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(reservationRepository.existsByMember_IdAndRoom_IdAndStatus(1L, 5L, ReservationStatus.CONFIRMED)).willReturn(true);
		given(reviewRepository.existsByMember_IdAndRoom_Id(1L, 5L)).willReturn(true);

		// when & then
		assertThrows(DuplicateReviewException.class, () -> reviewService.createReview("user1", request));
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("존재하지 않는 회원이면 UserNotFoundException이 발생한다")
	void createReview_실패_회원없음() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");
		given(memberRepository.findByUsername("unknown")).willReturn(Optional.empty());

		// when & then
		assertThrows(UserNotFoundException.class, () -> reviewService.createReview("unknown", request));
	}

	@Test
	@DisplayName("존재하지 않는 room이면 RoomNotFoundException이 발생한다")
	void createReview_실패_room없음() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(999L, 5, "최고예요");
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(RoomNotFoundException.class, () -> reviewService.createReview("user1", request));
	}

	@Test
	@DisplayName("본인이 작성한 리뷰는 정상적으로 수정된다")
	void updateReview_성공() {
		// given
		Review review = createReview(10L, member, room, 3, "그냥 그래요");
		ReviewUpdateRequest request = new ReviewUpdateRequest(5, "다시 보니 최고예요");

		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(reviewRepository.findById(10L)).willReturn(Optional.of(review));

		// when
		ReviewResponseDto result = reviewService.updateReview("user1", 10L, request);

		// then
		assertThat(result.rating()).isEqualTo(5);
		assertThat(result.content()).isEqualTo("다시 보니 최고예요");
	}

	@Test
	@DisplayName("본인이 작성하지 않은 리뷰를 수정하려 하면 ReviewAccessDeniedException이 발생한다")
	void updateReview_실패_권한없음() {
		// given
		Member otherMember = Member.builder().username("user2").password("password2").email("user2@test.com").name("회원2").build();
		ReflectionTestUtils.setField(otherMember, "id", 999L);

		Review review = createReview(10L, member, room, 3, "그냥 그래요"); // 작성자는 member(id=1L)
		ReviewUpdateRequest request = new ReviewUpdateRequest(5, "다시 보니 최고예요");

		given(memberRepository.findByUsername("user2")).willReturn(Optional.of(otherMember));
		given(reviewRepository.findById(10L)).willReturn(Optional.of(review));

		// when & then - user2가 남의 리뷰(작성자 user1)를 수정하려 함
		assertThrows(ReviewAccessDeniedException.class, () -> reviewService.updateReview("user2", 10L, request));
	}

	@Test
	@DisplayName("본인이 작성한 리뷰는 정상적으로 삭제된다")
	void deleteReview_성공() {
		// given
		Review review = createReview(10L, member, room, 3, "그냥 그래요");
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(reviewRepository.findById(10L)).willReturn(Optional.of(review));

		// when
		reviewService.deleteReview("user1", 10L);

		// then
		verify(reviewRepository, times(1)).delete(review);
	}

	@Test
	@DisplayName("본인이 작성하지 않은 리뷰를 삭제하려 하면 ReviewAccessDeniedException이 발생한다")
	void deleteReview_실패_권한없음() {
		// given
		Member otherMember = Member.builder().username("user2").password("password2").email("user2@test.com").name("회원2").build();
		ReflectionTestUtils.setField(otherMember, "id", 999L);

		Review review = createReview(10L, member, room, 3, "그냥 그래요");
		given(memberRepository.findByUsername("user2")).willReturn(Optional.of(otherMember));
		given(reviewRepository.findById(10L)).willReturn(Optional.of(review));

		// when & then
		assertThrows(ReviewAccessDeniedException.class, () -> reviewService.deleteReview("user2", 10L));
		verify(reviewRepository, never()).delete(any(Review.class));
	}

	@Test
	@DisplayName("회원이 작성한 리뷰 목록을 조회한다")
	void getReviewsByMember_성공() {
		// given
		Review review1 = createReview(10L, member, room, 5, "최고예요");
		given(reviewRepository.findByMemberId(1L)).willReturn(List.of(review1));

		// when
		List<ReviewResponseDto> result = reviewService.getReviewsByMember(1L);

		// then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("room의 리뷰를 평점순으로 페이징 조회한다")
	void getReviewsByRoom_성공() {
		// given
		Review review1 = createReview(10L, member, room, 5, "최고예요");
		Page<Review> page = new PageImpl<>(List.of(review1));
		given(reviewRepository.findByRoomIdOrderByRatingDesc(5L, PageRequest.of(0, 10))).willReturn(page);

		// when
		Page<ReviewResponseDto> result = reviewService.getReviewsByRoom(5L, PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	@DisplayName("리뷰가 있는 room의 평균 평점을 조회한다")
	void getRatingSummary_성공() {
		// given
		given(reviewRepository.findAverageRatingByRoomId(5L)).willReturn(4.0);
		given(reviewRepository.countByRoomId(5L)).willReturn(2L);

		// when
		RoomRatingSummaryDto result = reviewService.getRatingSummary(5L);

		// then
		assertThat(result.averageRating()).isEqualTo(4.0);
		assertThat(result.reviewCount()).isEqualTo(2L);
	}

	@Test
	@DisplayName("리뷰가 없는 room의 평균 평점은 0.0으로 처리된다")
	void getRatingSummary_리뷰없음() {
		// given - AVG는 대상 없으면 null 반환 (지난번 확인한 내용)
		given(reviewRepository.findAverageRatingByRoomId(5L)).willReturn(null);
		given(reviewRepository.countByRoomId(5L)).willReturn(0L);

		// when
		RoomRatingSummaryDto result = reviewService.getRatingSummary(5L);

		// then
		assertThat(result.averageRating()).isEqualTo(0.0); // null 방어 처리 확인
		assertThat(result.reviewCount()).isEqualTo(0L);
	}
}
