package com.mycom.myapp.domain.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
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
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.room.service.RoomServiceImpl;
import com.mycom.myapp.domain.wishlist.repository.WishlistRepository;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private WishlistRepository wishlistRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private RoomServiceImpl roomService;

	private Member vipMember;
	private Room room;

	@BeforeEach
	void setUp() {
		vipMember = Member.builder().username("user1").password("password1").email("user1@test.com").name("회원1").build();
		ReflectionTestUtils.setField(vipMember, "id", 1L);
		ReflectionTestUtils.setField(vipMember, "grade", MemberGrade.VIP);

		room = Room.builder().name("한강뷰 스튜디오").capacity(4).price(150000).build();
		ReflectionTestUtils.setField(room, "id", 5L);
	}

	@Test
	@DisplayName("room 단건 조회 시 할인가와 찜/평점 정보가 함께 반환된다")
	void getRoom_성공() {
		// given
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));
		given(wishlistRepository.existsByMember_IdAndRoom_Id(1L, 5L)).willReturn(true);
		given(wishlistRepository.countByRoomId(5L)).willReturn(3L);
		given(reviewRepository.findAverageRatingByRoomId(5L)).willReturn(4.5);
		given(reviewRepository.countByRoomId(5L)).willReturn(10L);

		// when
		RoomResponseDto result = roomService.getRoom("user1", 5L);

		// then
		assertThat(result.discountedPrice()).isEqualTo(MemberGrade.VIP.applyDiscount(150000));
		assertThat(result.wishlisted()).isTrue();
		assertThat(result.wishlistCount()).isEqualTo(3L);
		assertThat(result.averageRating()).isEqualTo(4.5);
		assertThat(result.reviewCount()).isEqualTo(10L);
	}

	@Test
	@DisplayName("리뷰가 없는 room을 조회하면 평균 평점은 0.0으로 처리된다")
	void getRoom_리뷰없음() {
		// given
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));
		given(wishlistRepository.existsByMember_IdAndRoom_Id(1L, 5L)).willReturn(false);
		given(wishlistRepository.countByRoomId(5L)).willReturn(0L);
		given(reviewRepository.findAverageRatingByRoomId(5L)).willReturn(null); // AVG는 리뷰 없으면 null
		given(reviewRepository.countByRoomId(5L)).willReturn(0L);

		// when
		RoomResponseDto result = roomService.getRoom("user1", 5L);

		// then
		assertThat(result.averageRating()).isEqualTo(0.0);
		assertThat(result.wishlisted()).isFalse();
	}

	@Test
	@DisplayName("존재하지 않는 room을 조회하면 RoomNotFoundException이 발생한다")
	void getRoom_실패_room없음() {
		// given
		given(roomRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(RoomNotFoundException.class, () -> roomService.getRoom("user1", 999L));
	}

	@Test
	@DisplayName("존재하지 않는 회원이 조회하면 UserNotFoundException이 발생한다")
	void getRoom_실패_회원없음() {
		// given
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(memberRepository.findByUsername("unknown")).willReturn(Optional.empty());

		// when & then
		assertThrows(UserNotFoundException.class, () -> roomService.getRoom("unknown", 5L));
	}

	@Test
	@DisplayName("room 목록 조회 시 배치 조회로 찜/평점 정보가 채워진다")
	void getRooms_성공() {
		// given
		Page<Room> page = new PageImpl<>(List.of(room));
		given(roomRepository.findAll(PageRequest.of(0, 10))).willReturn(page);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		given(wishlistRepository.findWishlistedRoomIds(1L, List.of(5L))).willReturn(List.of(5L));

		List<Object[]> wishlistCountRows = Collections.singletonList(new Object[]{5L, 3L});
		given(wishlistRepository.countByRoomIdIn(List.of(5L))).willReturn(wishlistCountRows);

		List<Object[]> ratingSummaryRows = Collections.singletonList(new Object[]{5L, 4.5, 10L});
		given(reviewRepository.findRatingSummaryByRoomIds(List.of(5L))).willReturn(ratingSummaryRows);

		// when
		Page<RoomResponseDto> result = roomService.getRooms("user1", PageRequest.of(0, 10));

		// then
		RoomResponseDto dto = result.getContent().get(0);
		assertThat(dto.wishlisted()).isTrue();
		assertThat(dto.wishlistCount()).isEqualTo(3L);
		assertThat(dto.averageRating()).isEqualTo(4.5);
		assertThat(dto.reviewCount()).isEqualTo(10L);

		verify(wishlistRepository, times(1)).findWishlistedRoomIds(any(), any());
		verify(wishlistRepository, times(1)).countByRoomIdIn(any());
		verify(reviewRepository, times(1)).findRatingSummaryByRoomIds(any());
	}

	@Test
	@DisplayName("검색 결과가 없으면 배치 조회 쿼리 자체를 호출하지 않는다")
	void getRooms_결과없음_배치조회스킵() {
		// given
		Page<Room> emptyPage = new PageImpl<>(List.of());
		given(roomRepository.findAll(PageRequest.of(0, 10))).willReturn(emptyPage);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		// when
		Page<RoomResponseDto> result = roomService.getRooms("user1", PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).isEmpty();
		verify(wishlistRepository, never()).findWishlistedRoomIds(any(), any());
		verify(reviewRepository, never()).findRatingSummaryByRoomIds(any());
	}

	@Test
	@DisplayName("조건에 맞는 room을 검색하면 배치 조회로 정보가 채워진다")
	void search_성공() {
		// given
		given(roomRepository.search("한강뷰", 4, 200000)).willReturn(List.of(room));
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));
		given(wishlistRepository.findWishlistedRoomIds(1L, List.of(5L))).willReturn(List.of());
		given(wishlistRepository.countByRoomIdIn(List.of(5L))).willReturn(List.of());
		given(reviewRepository.findRatingSummaryByRoomIds(List.of(5L))).willReturn(List.of());

		// when
		List<RoomResponseDto> result = roomService.search("user1", "한강뷰", 4, 200000);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).wishlisted()).isFalse();
		assertThat(result.get(0).wishlistCount()).isEqualTo(0L);
		assertThat(result.get(0).averageRating()).isEqualTo(0.0);
		assertThat(result.get(0).reviewCount()).isEqualTo(0L);
	}

	@Test
	@DisplayName("존재하지 않는 회원이 검색하면 UserNotFoundException이 발생한다")
	void search_실패_회원없음() {
		// given
		given(memberRepository.findByUsername("unknown")).willReturn(Optional.empty());

		// when & then
		assertThrows(UserNotFoundException.class, () -> roomService.search("unknown", "한강뷰", null, null));
	}

	@Test
	@DisplayName("조건에 맞는 room을 페이징 검색하면 배치 조회로 정보가 채워진다")
	void searchWithPaging_성공() {
		// given
		Page<Room> page = new PageImpl<>(List.of(room));
		given(roomRepository.search("한강뷰", 4, 200000, PageRequest.of(0, 10))).willReturn(page);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));
		given(wishlistRepository.findWishlistedRoomIds(1L, List.of(5L))).willReturn(List.of(5L));
		List<Object[]> wishlistCountRows = Collections.singletonList(new Object[]{5L, 1L});
		given(wishlistRepository.countByRoomIdIn(List.of(5L))).willReturn(wishlistCountRows);
		List<Object[]> ratingSummaryRows = Collections.singletonList(new Object[]{5L, 5.0, 1L});
		given(reviewRepository.findRatingSummaryByRoomIds(List.of(5L))).willReturn(ratingSummaryRows);

		// when
		Page<RoomResponseDto> result = roomService.searchWithPaging("user1", "한강뷰", 4, 200000, PageRequest.of(0, 10));

		// then
		assertThat(result.getContent().get(0).wishlisted()).isTrue();
		assertThat(result.getContent().get(0).averageRating()).isEqualTo(5.0);
	}

	@Test
	@DisplayName("room을 생성하면 찜/평점 정보는 기본값(0)으로 반환된다")
	void createRoom_성공() {
		// given
		RoomCreateRequest request = new RoomCreateRequest("한강뷰 스튜디오", 4, 150000);
		given(roomRepository.save(any(Room.class))).willReturn(room);

		// when
		RoomResponseDto result = roomService.createRoom(request);

		// then
		assertThat(result.name()).isEqualTo("한강뷰 스튜디오");
		assertThat(result.discountedPrice()).isNull();
		assertThat(result.wishlisted()).isFalse();
		assertThat(result.wishlistCount()).isEqualTo(0L);
		assertThat(result.averageRating()).isEqualTo(0.0);
		assertThat(result.reviewCount()).isEqualTo(0L);
		verify(roomRepository, times(1)).save(any(Room.class));
	}

	@Test
	@DisplayName("room을 수정하면 변경된 값이 반영되고 찜/평점 정보는 기본값이다")
	void updateRoom_성공() {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("새 이름", 6, 200000);
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));

		// when
		RoomResponseDto result = roomService.updateRoom(5L, request);

		// then
		assertThat(result.name()).isEqualTo("새 이름");
		assertThat(result.capacity()).isEqualTo(6);
		assertThat(result.price()).isEqualTo(200000);
		assertThat(result.wishlistCount()).isEqualTo(0L);
	}

	@Test
	@DisplayName("존재하지 않는 room을 수정하려 하면 RoomNotFoundException이 발생한다")
	void updateRoom_실패_존재하지_않음() {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("새 이름", 6, 200000);
		given(roomRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(999L, request));
	}

	@Test
	@DisplayName("room을 삭제하면 repository의 delete가 호출된다")
	void deleteRoom_성공() {
		// given
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));

		// when
		roomService.deleteRoom(5L);

		// then
		verify(roomRepository, times(1)).delete(room);
	}

	@Test
	@DisplayName("존재하지 않는 room을 삭제하려 하면 RoomNotFoundException이 발생하고 delete는 호출되지 않는다")
	void deleteRoom_실패_존재하지_않음() {
		// given
		given(roomRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(999L));
		verify(roomRepository, never()).delete(any(Room.class));
	}
}