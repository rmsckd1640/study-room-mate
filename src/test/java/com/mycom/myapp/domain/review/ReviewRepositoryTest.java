package com.mycom.myapp.domain.review;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.review.entity.Review;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.config.JpaAuditingConfig;

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = {ReviewRepository.class, RoomRepository.class, MemberRepository.class})
@Import(JpaAuditingConfig.class)
class ReviewRepositoryTest {

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member member1;
	private Member member2;
	private Room room1;
	private Room room2;

	@BeforeEach
	void setUp() {
		// 주의: Member.builder()의 실제 필드는 팀원 코드에 맞게 수정 필요
		member1 = memberRepository.save(Member.builder().username("user1").password("password1").email("user1@test.com").name("회원1").build());

		member2 = memberRepository.save(Member.builder().username("user2").password("password2").email("user2@test.com").name("회원2").build());

		room1 = roomRepository.save(Room.builder().name("한강뷰 스튜디오").location("서울 성동구").capacity(4).price(150000).build());

		room2 = roomRepository.save(Room.builder().name("해운대 오션뷰").location("부산 해운대구").capacity(6).price(300000).build());
	}

	private Review createReview(Member member, Room room, Integer rating, String content) {
		return Review.builder().member(member).room(room).rating(rating).content(content).build();
	}

	@Test
	@DisplayName("회원 id로 작성한 리뷰 목록을 조회한다")
	void findByMemberId_성공() {
		// given
		reviewRepository.save(createReview(member1, room1, 5, "최고예요"));
		reviewRepository.save(createReview(member1, room2, 3, "그냥 그래요"));
		reviewRepository.save(createReview(member2, room1, 4, "좋아요"));

		// when
		List<Review> result = reviewRepository.findByMemberId(member1.getId());

		// then
		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("room id로 작성된 리뷰 목록을 조회한다")
	void findByRoomId_성공() {
		// given
		reviewRepository.save(createReview(member1, room1, 5, "최고예요"));
		reviewRepository.save(createReview(member2, room1, 4, "좋아요"));
		reviewRepository.save(createReview(member1, room2, 2, "별로예요"));

		// when
		List<Review> result = reviewRepository.findByRoomId(room1.getId());

		// then
		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("room의 리뷰를 평점 높은 순으로 페이징 조회한다")
	void findByRoomIdOrderByRatingDesc_성공() {
		// given
		reviewRepository.save(createReview(member1, room1, 2, "별로예요"));
		reviewRepository.save(createReview(member2, room1, 5, "최고예요"));

		// when
		Page<Review> result = reviewRepository.findByRoomIdOrderByRatingDesc(room1.getId(), PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).getRating()).isEqualTo(5); // 평점 높은 순 첫 번째
		assertThat(result.getContent().get(1).getRating()).isEqualTo(2);
	}

	@Test
	@DisplayName("room에 달린 리뷰 개수를 센다")
	void countByRoomId_성공() {
		// given
		reviewRepository.save(createReview(member1, room1, 5, "최고예요"));
		reviewRepository.save(createReview(member2, room1, 4, "좋아요"));

		// when
		long count = reviewRepository.countByRoomId(room1.getId());

		// then
		assertThat(count).isEqualTo(2);
	}

	@Test
	@DisplayName("이미 리뷰를 작성했는지 확인한다")
	void existsByMemberIdAndRoomId_성공() {
		// given
		reviewRepository.save(createReview(member1, room1, 5, "최고예요"));

		// when & then
		assertThat(reviewRepository.existsByMember_IdAndRoom_Id(member1.getId(), room1.getId())).isTrue();
		assertThat(reviewRepository.existsByMember_IdAndRoom_Id(member2.getId(), room1.getId())).isFalse();
	}

	@Test
	@DisplayName("room의 평균 평점을 계산한다")
	void findAverageRatingByRoomId_성공() {
		// given
		reviewRepository.save(createReview(member1, room1, 4, "좋아요"));
		reviewRepository.save(createReview(member2, room1, 2, "별로예요"));
		// 평균 = (4 + 2) / 2 = 3.0

		// when
		Double average = reviewRepository.findAverageRatingByRoomId(room1.getId());

		// then
		assertThat(average).isEqualTo(3.0);
	}

	@Test
	@DisplayName("리뷰가 없는 room의 평균 평점은 null이다")
	void findAverageRatingByRoomId_리뷰없음() {
		// when
		Double average = reviewRepository.findAverageRatingByRoomId(room2.getId());

		// then
		assertThat(average).isNull();
	}

	@Test
	@DisplayName("삭제된 리뷰는 조회 결과와 평균 계산에서 제외된다")
	void 소프트_삭제된_리뷰는_제외된다() {
		// given
		Review review = reviewRepository.save(createReview(member1, room1, 5, "최고예요"));
		reviewRepository.save(createReview(member2, room1, 3, "보통이에요"));

		// when
		reviewRepository.delete(review);

		// then
		List<Review> result = reviewRepository.findByRoomId(room1.getId());
		assertThat(result).hasSize(1);

		Double average = reviewRepository.findAverageRatingByRoomId(room1.getId());
		assertThat(average).isEqualTo(3.0); // 삭제된 5점 리뷰는 제외되고 3점만 반영
	}
}