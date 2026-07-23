package com.mycom.myapp.domain.room;

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
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.room.service.RoomServiceImpl;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private MemberRepository memberRepository;

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
	@DisplayName("room 단건 조회 시 회원 등급 할인가가 적용된다")
	void getRoom_성공() {
		// given
		given(roomRepository.findById(5L)).willReturn(Optional.of(room));
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		// when
		RoomResponseDto result = roomService.getRoom("user1", 5L);

		// then
		assertThat(result.name()).isEqualTo("한강뷰 스튜디오");
		assertThat(result.discountedPrice()).isEqualTo(MemberGrade.VIP.applyDiscount(150000));
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
	@DisplayName("room 목록 조회 시 회원 등급 할인가가 적용된다")
	void getRooms_성공() {
		// given
		Page<Room> page = new PageImpl<>(List.of(room));
		given(roomRepository.findAll(PageRequest.of(0, 10))).willReturn(page);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		// when
		Page<RoomResponseDto> result = roomService.getRooms("user1", PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).discountedPrice()).isEqualTo(MemberGrade.VIP.applyDiscount(150000));
	}

	@Test
	@DisplayName("조건에 맞는 room을 검색하면 할인가가 적용된다")
	void search_성공() {
		// given
		given(roomRepository.search("한강뷰", 4, 200000)).willReturn(List.of(room));
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		// when
		List<RoomResponseDto> result = roomService.search("user1", "한강뷰", 4, 200000);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).discountedPrice()).isEqualTo(MemberGrade.VIP.applyDiscount(150000));
	}

	@Test
	@DisplayName("조건이 전부 null이어도 검색이 정상 동작한다")
	void search_조건없음() {
		// given
		given(roomRepository.search(null, null, null)).willReturn(List.of(room));
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		// when
		List<RoomResponseDto> result = roomService.search("user1", null, null, null);

		// then
		assertThat(result).hasSize(1);
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
	@DisplayName("조건에 맞는 room을 페이징 검색하면 할인가가 적용된다")
	void searchWithPaging_성공() {
		// given
		Page<Room> page = new PageImpl<>(List.of(room));
		given(roomRepository.search("한강뷰", 4, 200000, PageRequest.of(0, 10))).willReturn(page);
		given(memberRepository.findByUsername("user1")).willReturn(Optional.of(vipMember));

		// when
		Page<RoomResponseDto> result = roomService.searchWithPaging("user1", "한강뷰", 4, 200000, PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).discountedPrice()).isEqualTo(MemberGrade.VIP.applyDiscount(150000));
	}

	@Test
	@DisplayName("room을 생성하면 할인가 없이(null) 저장된 값이 반환된다")
	void createRoom_성공() {
		// given
		RoomCreateRequest request = new RoomCreateRequest("한강뷰 스튜디오", 4, 150000);
		given(roomRepository.save(any(Room.class))).willReturn(room);

		// when
		RoomResponseDto result = roomService.createRoom(request);

		// then
		assertThat(result.name()).isEqualTo("한강뷰 스튜디오");
		assertThat(result.discountedPrice()).isNull();
		verify(roomRepository, times(1)).save(any(Room.class));
	}

	@Test
	@DisplayName("room을 수정하면 변경된 값이 반영되고 할인가는 null이다")
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
		assertThat(result.discountedPrice()).isNull();
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