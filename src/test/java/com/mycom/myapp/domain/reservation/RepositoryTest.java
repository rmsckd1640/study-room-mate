package com.mycom.myapp.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryTest {

	@Autowired
	ReservationRepository reservationRepository;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	RoomRepository roomRepository;
	
	@Autowired
	RandomUtils random;
	
	@Test
	@Order(1)
	public void testValidate() {
		Room room = random.randomRoom();
		Member member = random.randomMember();
		
		memberRepository.save(member);
		roomRepository.save(room);
		
	    LocalDate reservationDate = LocalDate.of(2026, 7, 20);
	    LocalDateTime startTime = LocalDateTime.of(2026, 7, 20, 10, 0);
	    LocalDateTime endTime = LocalDateTime.of(2026, 7, 20, 11, 0);
		
		Reservation reservation = Reservation.builder()
												.member(member)
												.room(room)
												.status(ReservationStatus.PENDING)
												.startTime(startTime)
												.endTime(endTime)
												.reservationDate(reservationDate)
												.build(); 
		
		reservationRepository.save(reservation);
		
		assertNotNull(reservation);
		
		// CASE1 : 같은 방, 동일 시간 - 겹침
		boolean case1 = reservationRepository.existsOverlappingReservation(room.getId(), startTime, endTime);
		assertTrue(case1);
		
		// CASE2 : 다른 방, 같은 시간 - 안겹침
		boolean case2 = reservationRepository.existsOverlappingReservation(room.getId() + 1, startTime, endTime);
		assertFalse(case2);
		
		// CASE3 : 같은 방, 기존 예약 시작 전 - 안겹침
		boolean case3 = reservationRepository.existsOverlappingReservation(
				room.getId() + 1, 
				LocalDateTime.of(2026, 7, 20, 9, 0), 
				LocalDateTime.of(2026, 7, 20, 10, 0)
		);
		assertFalse(case3);
		
		// CASE4 : 같은 방, 기존 예약 시작 후 - 안겹침
		boolean case4 = reservationRepository.existsOverlappingReservation(
				room.getId() + 1, 
				LocalDateTime.of(2026, 7, 20, 11, 0), 
				LocalDateTime.of(2026, 7, 20, 12, 0)
		);
		assertFalse(case4);
		
	    // case5: 같은 방, 앞부분 겹침 -> 겹침
	    boolean case5 = reservationRepository.existsOverlappingReservation(
	            room.getId(),
	            LocalDateTime.of(2026, 7, 20, 9, 30),
	            LocalDateTime.of(2026, 7, 20, 10, 30));
	    assertTrue(case5);

	    // case6: 같은 방, 완전 포함하는 범위 -> 겹침
	    boolean case6 = reservationRepository.existsOverlappingReservation(
	            room.getId(),
	            LocalDateTime.of(2026, 7, 20, 9, 0),
	            LocalDateTime.of(2026, 7, 20, 12, 0));
	    assertTrue(case6);
	}
	
	@Test
	@Order(2)
	public void testInsert() {
		Room room = random.randomRoom();
		Member member = random.randomMember();
		
	    memberRepository.save(member);
	    roomRepository.save(room);
	    
	    LocalDateTime startTime = LocalDateTime.of(2026, 7, 20, 10, 0);
	    LocalDateTime endTime = LocalDateTime.of(2026, 7, 20, 11, 0);
	    LocalDate reservationDate = LocalDate.of(2026, 7, 20);
		
		assertFalse(reservationRepository.existsOverlappingReservation(room.getId(), startTime, endTime));
		
		Reservation reservation = Reservation.builder()
												.member(member)
												.room(room)
												.status(ReservationStatus.PENDING)
												.reservationDate(reservationDate)
												.startTime(startTime)
												.endTime(endTime)
												.build();
		
		reservation = reservationRepository.save(reservation);
		
		assertNotNull(reservation);
	}
	
	@Test
	@Order(3)
	public void testInsertTX() {
		Room room = roomRepository.save(random.randomRoom());
		
	    int threadCount = 10;
	    ExecutorService executor = Executors.newFixedThreadPool(4);
	    CountDownLatch latch = new CountDownLatch(threadCount);

	    AtomicInteger successCount = new AtomicInteger();
	    AtomicInteger failCount = new AtomicInteger();
	    
		
		for (int i = 0; i < threadCount; i++) {
			int taskId = i;
			
			Member member = memberRepository.save(random.randomMember());
			
			executor.submit(() -> {
				try {
	                log.info("TASK {}", taskId);

	                Reservation reservation = Reservation.builder()
	                										.room(room)
	                										.member(member)
	                										.startTime(null)
	                										.endTime(null)
	                										.reservationDate(null)
	                										.status(ReservationStatus.PENDING)
	                										.build();
	                
	                reservationRepository.save(reservation);

	                successCount.incrementAndGet();
				} catch (Exception e) {
					
				} finally {
					latch.countDown();
				}
			});
		}
		
	    // 2. 모든 스레드가 끝날 때까지 대기
	    latch.await(10, TimeUnit.SECONDS);
	    executor.shutdown();

	    // 3. 검증: 같은 시간대 예약이므로 단 1건만 성공해야 함
	    log.info("성공: {}, 실패: {}", successCount.get(), failCount.get());
	    assertEquals(1, successCount.get());
	    assertEquals(threadCount - 1, failCount.get());
	}
	
}
