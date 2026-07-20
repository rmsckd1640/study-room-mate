package com.mycom.myapp.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.RandomUtils;

import jakarta.transaction.Transactional;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryTest {

	@Autowired
	ReservationRepository reservationRepository;
	
	@Autowired
	RandomUtils random;
	
	@Test
	@Order(1)
	@Transactional
	public void testValidate() {
		Room room = random.randomRoom();
		
	    LocalDate reservationDate = LocalDate.of(2026, 7, 20);
	    LocalDateTime startTime = LocalDateTime.of(2026, 7, 20, 10, 0);
	    LocalDateTime endTime = LocalDateTime.of(2026, 7, 20, 11, 0);
		
		Reservation reservation = Reservation.builder()
												.member(random.randomMember())
												.room(room)
												.status(ReservationStatus.PENDING)
												.startTime(startTime)
												.endTime(endTime)
												.reservationDate(reservationDate)
												.build(); 
		
		reservation = reservationRepository.save(reservation);
		
		assertNotNull(reservation);
		
		// CASE1 : 같은 방, 동일 시간 - 겹침
		boolean case1 = reservationRepository.existsOverlappingReservation(room.getId(), startTime, endTime);
		assertTrue(case1);
		
		// CASE2 : 다른 방, 같은 시간 - 안겹침
		boolean case2 = reservationRepository.existsOverlappingReservation(room.getId() + 1, startTime, endTime);
		assertFalse(case2);
		
		// CASE2 : 같은 방, 기존 예약 - 안겹침
		boolean case3 = reservationRepository.existsOverlappingReservation(room.getId() + 1, startTime, endTime);
		assertFalse(case2);
	}
	
}
