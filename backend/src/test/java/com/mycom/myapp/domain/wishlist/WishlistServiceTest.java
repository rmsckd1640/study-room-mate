package com.mycom.myapp.domain.wishlist;

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
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.wishlist.dto.WishlistCreateRequest;
import com.mycom.myapp.domain.wishlist.dto.WishlistResponseDto;
import com.mycom.myapp.domain.wishlist.entity.Wishlist;
import com.mycom.myapp.domain.wishlist.repository.WishlistRepository;
import com.mycom.myapp.domain.wishlist.service.WishlistServiceImpl;
import com.mycom.myapp.global.exception.DuplicateWishlistException;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;
import com.mycom.myapp.global.exception.WishlistNotFoundException;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

	@Mock
	private WishlistRepository wishlistRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private RoomRepository roomRepository;

	@InjectMocks
	private WishlistServiceImpl wishlistService;

	private Member member;
	private Room room;

	@BeforeEach
	void setUp() {
		member = Member.builder().username("user1").password("password1").email("user1@test.com").name("회원1").build();
		ReflectionTestUtils.setField(member, "id", 1L);

		room = Room.builder().name("한강뷰 스튜디오").capacity(4).price(150000).build();
		ReflectionTestUtils.setField(room, "id", 5L);
	}

	private Wishlist createWishlist(Long id, Member member, Room room) {
		Wishlist wishlist = Wishlist.builder().member(member).room(room).build();
		ReflectionTestUtils.setField(wishlist, "id", id);
		return wishlist;
	}

	@Test
	@DisplayName("중복이 없으면 찜이 정상적으로 생성된다")
	void createWishlist_성공() {
		// given
		WishlistCreateRequest request = new WishlistCreateRequest(5L);
		Wishlist savedWishlist = createWishlist(10L, member, room);

		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(wishlistRepository.existsByMember_IdAndRoom_Id(1L, 5L)).willReturn(false);
		given(wishlistRepository.save(any(Wishlist.class))).willReturn(savedWishlist);

		// when
		WishlistResponseDto result = wishlistService.createWishlist("user1", request);

		// then
		assertThat(result.roomId()).isEqualTo(5L);
		assertThat(result.memberId()).isEqualTo(1L);
		verify(wishlistRepository, times(1)).save(any(Wishlist.class));
	}

	@Test
	@DisplayName("이미 찜한 방이면 DuplicateWishlistException이 발생한다")
	void createWishlist_실패_중복() {
		// given
		WishlistCreateRequest request = new WishlistCreateRequest(5L);

		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(wishlistRepository.existsByMember_IdAndRoom_Id(1L, 5L)).willReturn(true);

		// when & then
		assertThrows(DuplicateWishlistException.class, () -> wishlistService.createWishlist("user1", request));
		verify(wishlistRepository, never()).save(any(Wishlist.class));
	}

	@Test
	@DisplayName("존재하지 않는 회원이면 UserNotFoundException이 발생한다")
	void createWishlist_실패_회원없음() {
		// given
		WishlistCreateRequest request = new WishlistCreateRequest(5L);
		given(memberRepository.findByUsername("unknown")).willReturn(Optional.empty());

		// when & then
		assertThrows(UserNotFoundException.class, () -> wishlistService.createWishlist("unknown", request));
	}

	@Test
	@DisplayName("존재하지 않는 room이면 RoomNotFoundException이 발생한다")
	void createWishlist_실패_room없음() {
		// given
		WishlistCreateRequest request = new WishlistCreateRequest(999L);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(roomRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(RoomNotFoundException.class, () -> wishlistService.createWishlist("user1", request));
	}

	@Test
	@DisplayName("찜한 기록이 있으면 정상적으로 삭제된다")
	void deleteWishlist_성공() {
		// given
		Wishlist wishlist = createWishlist(10L, member, room);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(wishlistRepository.findByMember_IdAndRoom_Id(1L, 5L)).willReturn(Optional.of(wishlist));

		// when
		wishlistService.deleteWishlist("user1", 5L);

		// then
		verify(wishlistRepository, times(1)).delete(wishlist);
	}

	@Test
	@DisplayName("찜한 기록이 없으면 WishlistNotFoundException이 발생한다")
	void deleteWishlist_실패_기록없음() {
		// given
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(wishlistRepository.findByMember_IdAndRoom_Id(1L, 5L)).willReturn(Optional.empty());

		// when & then
		assertThrows(WishlistNotFoundException.class, () -> wishlistService.deleteWishlist("user1", 5L));
		verify(wishlistRepository, never()).delete(any(Wishlist.class));
	}

	@Test
	@DisplayName("존재하지 않는 회원이 삭제를 시도하면 UserNotFoundException이 발생한다")
	void deleteWishlist_실패_회원없음() {
		// given
		given(memberRepository.findByUsername("unknown")).willReturn(Optional.empty());

		// when & then
		assertThrows(UserNotFoundException.class, () -> wishlistService.deleteWishlist("unknown", 5L));
	}

	@Test
	@DisplayName("회원이 찜한 목록을 조회한다")
	void getWishlistsByMember_성공() {
		// given
		Wishlist wishlist = createWishlist(10L, member, room);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));
		given(wishlistRepository.findByMemberId(1L)).willReturn(List.of(wishlist));

		// when
		List<WishlistResponseDto> result = wishlistService.getWishlistsByMember("user1");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).roomId()).isEqualTo(5L);
	}

	@Test
	@DisplayName("room이 찜당한 개수를 센다")
	void countByRoomId_성공() {
		// given
		given(wishlistRepository.countByRoomId(5L)).willReturn(7L);

		// when
		long result = wishlistService.countByRoomId(5L);

		// then
		assertThat(result).isEqualTo(7L);
	}
}
