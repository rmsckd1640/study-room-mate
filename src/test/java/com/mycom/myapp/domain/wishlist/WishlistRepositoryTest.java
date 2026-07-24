package com.mycom.myapp.domain.wishlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.wishlist.entity.Wishlist;
import com.mycom.myapp.domain.wishlist.repository.WishlistRepository;
import com.mycom.myapp.global.config.JpaAuditingConfig;
import com.mycom.myapp.global.config.QuerydslConfig;

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = {WishlistRepository.class, RoomRepository.class, MemberRepository.class})
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class WishlistRepositoryTest {

	@Autowired
	private WishlistRepository wishlistRepository;

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
		member1 = memberRepository.save(Member.builder().username("user1").password("password1").email("user1@test.com").name("회원1").role(MemberRole.USER).build());

		member2 = memberRepository.save(Member.builder().username("user2").password("password2").email("user2@test.com").name("회원2").role(MemberRole.USER).build());

		room1 = roomRepository.save(Room.builder().name("한강뷰 스튜디오").capacity(4).price(150000).build());

		room2 = roomRepository.save(Room.builder().name("해운대 오션뷰").capacity(6).price(300000).build());
	}

	private Wishlist createWishlist(Member member, Room room) {
		return Wishlist.builder().member(member).room(room).build();
	}

	@Test
	@DisplayName("회원 id로 찜한 목록을 조회한다")
	void findByMemberId_성공() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));
		wishlistRepository.save(createWishlist(member1, room2));
		wishlistRepository.save(createWishlist(member2, room1));

		// when
		List<Wishlist> result = wishlistRepository.findByMemberId(member1.getId());

		// then
		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("회원-room 조합으로 찜 단건을 조회한다")
	void findByMemberIdAndRoomId_성공() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));

		// when
		Optional<Wishlist> result = wishlistRepository.findByMember_IdAndRoom_Id(member1.getId(), room1.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getRoom().getId()).isEqualTo(room1.getId());
	}

	@Test
	@DisplayName("찜하지 않은 조합으로 조회하면 빈 Optional을 반환한다")
	void findByMemberIdAndRoomId_찜안함() {
		// when
		Optional<Wishlist> result = wishlistRepository.findByMember_IdAndRoom_Id(member1.getId(), room1.getId());

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("이미 찜했는지 여부를 확인한다")
	void existsByMemberIdAndRoomId_성공() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));

		// when & then
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member1.getId(), room1.getId())).isTrue();
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member1.getId(), room2.getId())).isFalse();
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member2.getId(), room1.getId())).isFalse();
	}

	@Test
	@DisplayName("room이 찜된 횟수를 센다")
	void countByRoomId_성공() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));
		wishlistRepository.save(createWishlist(member2, room1));
		wishlistRepository.save(createWishlist(member1, room2));

		// when
		long room1Count = wishlistRepository.countByRoomId(room1.getId());
		long room2Count = wishlistRepository.countByRoomId(room2.getId());

		// then
		assertThat(room1Count).isEqualTo(2);
		assertThat(room2Count).isEqualTo(1);
	}

	@Test
	@DisplayName("같은 회원-room 조합을 중복으로 찜하면 UNIQUE 제약 위반이 발생한다")
	void 중복_찜은_예외가_발생한다() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> wishlistRepository.saveAndFlush(createWishlist(member1, room1)));
	}

	@Test
	@DisplayName("찜을 삭제하면 물리적으로 데이터가 사라진다")
	void deleteWishlist_물리삭제_확인() {
		// given
		Wishlist wishlist = wishlistRepository.save(createWishlist(member1, room1));
		Long wishlistId = wishlist.getId();

		// when
		wishlistRepository.delete(wishlist);

		// then
		assertThat(wishlistRepository.findById(wishlistId)).isEmpty();
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member1.getId(), room1.getId())).isFalse();
	}

	@Test
	@DisplayName("찜을 삭제한 후 같은 조합으로 다시 찜할 수 있다 (물리 삭제이므로 UNIQUE 제약과 충돌 없음)")
	void 삭제후_재찜_가능() {
		// given
		Wishlist wishlist = wishlistRepository.save(createWishlist(member1, room1));
		wishlistRepository.delete(wishlist);
		wishlistRepository.flush();

		// when
		Wishlist newWishlist = wishlistRepository.saveAndFlush(createWishlist(member1, room1));

		// then
		assertThat(newWishlist.getId()).isNotNull();
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member1.getId(), room1.getId())).isTrue();
	}

	@Test
	@DisplayName("여러 room에 대해 찜한 room id만 골라낸다")
	void findWishlistedRoomIds_성공() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));
		// room2는 찜 안 함

		// when
		List<Long> result = wishlistRepository.findWishlistedRoomIds(member1.getId(), List.of(room1.getId(), room2.getId()));

		// then
		assertThat(result).containsExactly(room1.getId());
	}

	@Test
	@DisplayName("찜한 게 하나도 없으면 빈 리스트를 반환한다")
	void findWishlistedRoomIds_찜없음() {
		// when
		List<Long> result = wishlistRepository.findWishlistedRoomIds(member1.getId(), List.of(room1.getId(), room2.getId()));

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("여러 room의 찜 개수를 한 번에 집계한다")
	void countByRoomIdIn_성공() {
		// given
		wishlistRepository.save(createWishlist(member1, room1));
		wishlistRepository.save(createWishlist(member2, room1));
		wishlistRepository.save(createWishlist(member1, room2));

		// when
		List<Object[]> result = wishlistRepository.countByRoomIdIn(List.of(room1.getId(), room2.getId()));

		// then
		Map<Long, Long> countMap = result.stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

		assertThat(countMap.get(room1.getId())).isEqualTo(2L);
		assertThat(countMap.get(room2.getId())).isEqualTo(1L);
	}
}