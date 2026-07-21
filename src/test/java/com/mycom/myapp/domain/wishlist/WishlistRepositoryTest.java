package com.mycom.myapp.domain.wishlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

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

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = {WishlistRepository.class, RoomRepository.class, MemberRepository.class})
@Import(JpaAuditingConfig.class)
public class WishlistRepositoryTest {

	@Autowired
	private WishlistRepository wishlistRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member member;
	private Room room1;
	private Room room2;

	@BeforeEach
	void setUp() {
		member = memberRepository
				.save(Member
						.builder().username("Test").password("test").email("test@test.com").name("testname").role(MemberRole.USER) // null 대신 실제 값 (아래 설명 참고)
						.build());

		room1 = roomRepository.save(Room.builder().name("testname1").capacity(101).price(10001).build());

		room2 = roomRepository.save(Room.builder().name("testname2").capacity(102).price(10002).build());
	}

	private Wishlist createWishlist(Member member, Room room) {
		return Wishlist.builder().member(member).room(room).build();
	}

	@Test
	@DisplayName("즐겨찾기를 추가하고 회원 id로 조회한다")
	void createWishlistAndFindByMemberId() {
		// given
		wishlistRepository.save(createWishlist(member, room1));
		wishlistRepository.save(createWishlist(member, room2));

		// when
		List<Wishlist> result = wishlistRepository.findByMemberId(member.getId());

		// then
		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("이미 찜한 방을 다시 찜하려 하면 UNIQUE 제약으로 실패한다")
	void 중복_찜은_예외가_발생한다() {
		// given
		wishlistRepository.save(createWishlist(member, room1));

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> wishlistRepository.saveAndFlush(createWishlist(member, room1)));
	}

	@Test
	@DisplayName("찜 여부를 확인한다")
	void existsByMemberIdAndRoomId_성공() {
		// given
		wishlistRepository.save(createWishlist(member, room1));

		// when & then
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member.getId(), room1.getId())).isTrue();
		assertThat(wishlistRepository.existsByMember_IdAndRoom_Id(member.getId(), room2.getId())).isFalse();
	}

	@Test
	@DisplayName("회원이 찜한 개수를 센다")
	void countByMemberId_성공() {
		// given
		wishlistRepository.save(createWishlist(member, room1));
		wishlistRepository.save(createWishlist(member, room2));

		// when
		long count = wishlistRepository.countByMemberId(member.getId());

		// then
		assertThat(count).isEqualTo(2);
	}
}
