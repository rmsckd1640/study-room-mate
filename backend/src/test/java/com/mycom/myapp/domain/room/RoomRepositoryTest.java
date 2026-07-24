package com.mycom.myapp.domain.room;

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

import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.config.JpaAuditingConfig;
import com.mycom.myapp.global.config.QuerydslConfig;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class RoomRepositoryTest {

	@Autowired
	private RoomRepository roomRepository;

	private Room createRoom(String name, int capacity, int price) {
		return Room.builder().name(name).capacity(capacity).price(price).build();
	}

	@BeforeEach
	void setUp() {
		roomRepository.save(createRoom("한강뷰 스튜디오", 4, 150000));
		roomRepository.save(createRoom("성수동 루프탑", 2, 200000));
		roomRepository.save(createRoom("해운대 오션뷰", 6, 300000));
		roomRepository.save(createRoom("강남 한강뷰 오피스텔", 8, 100000));
	}

	@Test
	@DisplayName("이름 조건만으로 검색한다")
	void search_이름만() {
		// when
		List<Room> result = roomRepository.search("한강뷰", null, null);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Room::getName).allMatch(name -> name.contains("한강뷰"));
	}

	@Test
	@DisplayName("수용 인원 조건만으로 검색한다")
	void search_수용인원만() {
		// when
		List<Room> result = roomRepository.search(null, 6, null);

		// then
		assertThat(result).hasSize(2); // 해운대(6), 강남(8)
		assertThat(result).extracting(Room::getCapacity).allMatch(capacity -> capacity >= 6);
	}

	@Test
	@DisplayName("가격 조건만으로 검색한다")
	void search_가격만() {
		// when
		List<Room> result = roomRepository.search(null, null, 150000);

		// then
		assertThat(result).hasSize(2); // 한강뷰(150000), 강남(100000)
		assertThat(result).extracting(Room::getPrice).allMatch(price -> price <= 150000);
	}

	@Test
	@DisplayName("이름과 가격 조건을 동시에 적용한다")
	void search_이름과가격_조합() {
		// when
		List<Room> result = roomRepository.search("한강뷰", null, 150000);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Room::getName).containsExactlyInAnyOrder("한강뷰 스튜디오", "강남 한강뷰 오피스텔");
	}

	@Test
	@DisplayName("세 조건을 모두 적용한다")
	void search_세조건_모두() {
		// when
		List<Room> result = roomRepository.search("한강뷰", 4, 200000);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Room::getName).containsExactlyInAnyOrder("한강뷰 스튜디오", "강남 한강뷰 오피스텔");
	}

	@Test
	@DisplayName("이름과 가격 조건으로 정확히 하나만 걸러낸다")
	void search_이름과가격_단일결과() {
		// when - 강남 한강뷰 오피스텔(100000)은 제외되도록 가격을 더 좁힘
		List<Room> result = roomRepository.search("한강뷰", null, 120000);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("강남 한강뷰 오피스텔");
	}

	@Test
	@DisplayName("조건이 하나도 없으면 전체를 반환한다")
	void search_조건없음() {
		// when
		List<Room> result = roomRepository.search(null, null, null);

		// then
		assertThat(result).hasSize(4);
	}

	@Test
	@DisplayName("조건에 맞는 room이 없으면 빈 리스트를 반환한다")
	void search_결과없음() {
		// when
		List<Room> result = roomRepository.search("존재하지않는이름", null, null);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Page 버전 - 조건과 페이징이 함께 적용된다")
	void search_페이징_성공() {
		// when
		Page<Room> result = roomRepository.search("한강뷰", null, null, PageRequest.of(0, 1));

		// then
		assertThat(result.getContent()).hasSize(1); // 페이지 크기 1이라 1개만
		assertThat(result.getTotalElements()).isEqualTo(2); // 전체는 2개("한강뷰" 포함 2건)
		assertThat(result.getTotalPages()).isEqualTo(2);
	}

	@Test
	@DisplayName("Page 버전 - 조건 없이 전체를 페이징 조회한다")
	void search_페이징_조건없음() {
		// when
		Page<Room> result = roomRepository.search(null, null, null, PageRequest.of(0, 2));

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(4);
		assertThat(result.getTotalPages()).isEqualTo(2);
	}
}