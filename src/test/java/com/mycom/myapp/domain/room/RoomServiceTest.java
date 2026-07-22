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

import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.room.service.RoomServiceImpl;
import com.mycom.myapp.global.exception.RoomNotFoundException;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;

	@InjectMocks
	private RoomServiceImpl roomService;

	private Room room;

	@BeforeEach
	void setUp() {
		room = Room.builder().name("한강뷰 스튜디오").capacity(4).price(150000).build();
		ReflectionTestUtils.setField(room, "id", 1L);
	}

	@Test
	@DisplayName("존재하는 room을 id로 조회하면 정상적으로 반환한다")
	void getRoom_성공() {
		// given
		given(roomRepository.findById(1L)).willReturn(Optional.of(room));

		// when
		RoomResponseDto result = roomService.getRoom(1L);

		// then
		assertThat(result.name()).isEqualTo("한강뷰 스튜디오");
		assertThat(result.capacity()).isEqualTo(4);
		assertThat(result.price()).isEqualTo(150000);
	}

	@Test
	@DisplayName("존재하지 않는 id로 조회하면 RoomNotFoundException이 발생한다")
	void getRoom_실패_존재하지_않는_room() {
		// given
		given(roomRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(RoomNotFoundException.class, () -> roomService.getRoom(999L));
	}

	@Test
	@DisplayName("room을 생성하면 저장된 값이 반환된다")
	void createRoom_성공() {
		// given
		RoomCreateRequest request = new RoomCreateRequest("한강뷰 스튜디오", 4, 150000);
		given(roomRepository.save(any(Room.class))).willReturn(room);

		// when
		RoomResponseDto result = roomService.createRoom(request);

		// then
		assertThat(result.name()).isEqualTo("한강뷰 스튜디오");
		verify(roomRepository, times(1)).save(any(Room.class));
	}

	@Test
	@DisplayName("room 정보를 수정하면 변경된 값이 반영된다")
	void updateRoom_성공() {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("새 이름", 6, 200000);
		given(roomRepository.findById(1L)).willReturn(Optional.of(room));

		// when
		RoomResponseDto result = roomService.updateRoom(1L, request);

		// then
		assertThat(result.name()).isEqualTo("새 이름");
		assertThat(result.capacity()).isEqualTo(6);
		assertThat(result.price()).isEqualTo(200000);
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
	@DisplayName("잘못된 값으로 수정하면 IllegalArgumentException이 발생한다")
	void updateRoom_실패_검증오류() {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("", 6, 200000); // name이 빈 문자열
		given(roomRepository.findById(1L)).willReturn(Optional.of(room));

		// when & then
		assertThrows(IllegalArgumentException.class, () -> roomService.updateRoom(1L, request));
	}

	@Test
	@DisplayName("room을 삭제하면 repository의 delete가 호출된다")
	void deleteRoom_성공() {
		// given
		given(roomRepository.findById(1L)).willReturn(Optional.of(room));

		// when
		roomService.deleteRoom(1L);

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

	@Test
	@DisplayName("이름으로 room을 검색한다")
	void searchByName_성공() {
		// given
		given(roomRepository.findByNameContaining("한강뷰")).willReturn(List.of(room));

		// when
		List<RoomResponseDto> result = roomService.searchByName("한강뷰");

		// then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("최소 수용 인원 이상인 room을 검색한다")
	void searchByMinCapacity_성공() {
		// given
		given(roomRepository.findByCapacityGreaterThanEqual(4)).willReturn(List.of(room));

		// when
		List<RoomResponseDto> result = roomService.searchByMinCapacity(4);

		// then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("최대 가격 이하인 room을 검색한다")
	void searchByMaxPrice_성공() {
		// given
		given(roomRepository.findByPriceLessThanEqual(200000)).willReturn(List.of(room));

		// when
		List<RoomResponseDto> result = roomService.searchByMaxPrice(200000);

		// then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("전체 room을 페이징 조회한다")
	void getRooms_성공() {
		// given
		Page<Room> page = new PageImpl<>(List.of(room));
		given(roomRepository.findAll(PageRequest.of(0, 10))).willReturn(page);

		// when
		Page<RoomResponseDto> result = roomService.getRooms(PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).hasSize(1);
	}
}