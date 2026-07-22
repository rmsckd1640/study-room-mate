package com.mycom.myapp.domain.room;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.config.JpaAuditingConfig;

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = RoomRepository.class)
@Import(JpaAuditingConfig.class)
class RoomRepositoryTest {

	@Autowired
	private RoomRepository roomRepository;

	private Room createRoom(String name, int capacity, int price) {
		return Room.builder().name(name).capacity(capacity).price(price).build();
	}

	@Test
	@DisplayName("이름에 특정 키워드가 포함된 room을 조회한다")
	void findByNameContaining_성공() {
		// given
		roomRepository.save(createRoom("한강뷰 스튜디오", 4, 150000));
		roomRepository.save(createRoom("성수동 루프탑", 2, 200000));

		// when
		List<Room> result = roomRepository.findByNameContaining("한강뷰");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("한강뷰 스튜디오");
	}

	@Test
	@DisplayName("특정 인원 수 이상을 수용하는 room만 조회한다")
	void findByCapacityGreaterThanEqual_성공() {
		// given
		roomRepository.save(createRoom("2인실", 2, 100000));
		roomRepository.save(createRoom("4인실", 4, 150000));
		roomRepository.save(createRoom("6인실", 6, 200000));

		// when
		List<Room> result = roomRepository.findByCapacityGreaterThanEqual(4);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Room::getCapacity).allMatch(capacity -> capacity >= 4);
	}

	@Test
	@DisplayName("특정 가격 이하인 room만 조회한다")
	void findByPriceLessThanEqual_성공() {
		// given
		roomRepository.save(createRoom("저가형", 2, 80000));
		roomRepository.save(createRoom("중가형", 4, 150000));
		roomRepository.save(createRoom("고가형", 6, 300000));

		// when
		List<Room> result = roomRepository.findByPriceLessThanEqual(150000);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Room::getPrice).allMatch(price -> price <= 150000);
	}

	@Test
	@DisplayName("페이징 조건에 맞게 전체 room을 조회한다")
	void findAll_페이징_성공() {
		// given
		for (int i = 1; i <= 15; i++) {
			roomRepository.save(createRoom("room" + i, 2, 100000));
		}

		// when
		Page<Room> result = roomRepository.findAll(PageRequest.of(0, 10));

		// then
		assertThat(result.getContent()).hasSize(10);
		assertThat(result.getTotalElements()).isEqualTo(15);
		assertThat(result.getTotalPages()).isEqualTo(2);
	}

	@Test
	@DisplayName("삭제된 room은 조회 결과에서 제외된다 (SQLRestriction 검증)")
	void 소프트_삭제된_room은_조회되지_않는다() {
		// given
		Room room = roomRepository.save(createRoom("삭제될 방", 2, 100000));
		Long roomId = room.getId();

		// when
		roomRepository.delete(room); // @SQLDelete가 UPDATE로 가로챔

		// then
		List<Room> result = roomRepository.findByNameContaining("삭제될 방");
		assertThat(result).isEmpty();

		// findById로도 확인
		assertThat(roomRepository.findById(roomId)).isEmpty();
	}
}
