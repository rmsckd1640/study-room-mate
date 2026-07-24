package com.mycom.myapp.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
public class ReservationRepositoryTest {

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
	public void testList() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());
		
	    LocalDateTime start = LocalDateTime.of(2026, 7, 20, 9, 0);
	    LocalDateTime end = LocalDateTime.of(2026, 7, 20, 15, 0);
	    
	    Reservation pending = reservationRepository.save(Reservation.builder()
	            .member(member).room(room)
	            .status(ReservationStatus.PENDING)
	            .reservationDate(LocalDate.of(2026, 7, 20))
	            .startTime(start).endTime(start.plusHours(1))
	            .build());

	    Reservation confirmed = reservationRepository.save(Reservation.builder()
	            .member(member).room(room)
	            .status(ReservationStatus.CONFIRMED)
	            .reservationDate(LocalDate.of(2026, 7, 20))
	            .startTime(start.plusHours(2)).endTime(start.plusHours(3))
	            .build());

	    Reservation cancelled = reservationRepository.save(Reservation.builder()
	            .member(member).room(room)
	            .status(ReservationStatus.CANCELLED)
	            .reservationDate(LocalDate.of(2026, 7, 20))
	            .startTime(end).endTime(end.plusHours(1))
	            .build());
		
	    // CASE 1. 스터디룸 아이디로 find
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<Reservation> lists1 = reservationRepository.findByRoomIdAndDeletedAtIsNull(room.getId(), pageable);
	    log.info("[STEP 1] : DATA : {}", lists1.getContent());
	    assertFalse(lists1.isEmpty());
	    assertTrue(lists1.stream().allMatch(r -> r.getRoom().getId().equals(room.getId())));

	    // CASE 2. 유저 아이디로 find
	    List<Reservation> lists2 = reservationRepository.findByMember_UsernameAndDeletedAtIsNull(member.getUsername());
	    log.info("[STEP 2] : DATA : {}", lists2);
	    assertFalse(lists2.isEmpty());
	    assertTrue(lists2.stream().allMatch(r -> r.getMember().getId().equals(member.getId())));

	    // CASE 3. 상태로 find
	    List<Reservation> lists3 = reservationRepository.findByStatus(ReservationStatus.PENDING);
	    List<Reservation> lists4 = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
	    List<Reservation> lists5 = reservationRepository.findByStatus(ReservationStatus.CANCELLED);
	    log.info("[STEP 3-1] : DATA : {}", lists3);
	    log.info("[STEP 3-2] : DATA : {}", lists4);
	    log.info("[STEP 3-3] : DATA : {}", lists5);
	    assertTrue(lists3.stream().anyMatch(r -> r.getId().equals(pending.getId())));
	    assertTrue(lists4.stream().anyMatch(r -> r.getId().equals(confirmed.getId())));
	    assertTrue(lists5.stream().anyMatch(r -> r.getId().equals(cancelled.getId())));

	    // CASE 4. 상태와 스터디룸 아이디로 find
	    List<Reservation> lists6 = reservationRepository.findByStatusAndRoomId(ReservationStatus.CANCELLED, room.getId());
	    log.info("[STEP 4] : DATA : {}", lists6);
	    assertTrue(lists6.stream().anyMatch(r -> r.getId().equals(cancelled.getId())));

	    // CASE 5. 상태와 시작시간이 지정한 시간이 지난 시점의 것만 find
	    List<Reservation> lists7 = reservationRepository.findByStatusAndDeletedAtIsNullAndStartTimeAfter(ReservationStatus.CANCELLED, start); // cancelled의 startTime(=end)이 start보다 뒤 → 포함
	    List<Reservation> lists8 = reservationRepository.findByStatusAndDeletedAtIsNullAndStartTimeAfter(ReservationStatus.CANCELLED, end.plusHours(1)); // cancelled의 startTime보다도 뒤 → 미포함
	    log.info("[STEP 5-1] : DATA : {}", lists7);
	    log.info("[STEP 5-2] : DATA : {}", lists8);
	    assertTrue(lists7.stream().anyMatch(r -> r.getId().equals(cancelled.getId())));
	    assertTrue(lists8.stream().noneMatch(r -> r.getId().equals(cancelled.getId())));

	    // CASE 6. 상태와 스터디룸 아이디 그리고 시작시간이 지난 시점의 것만 find
	    List<Reservation> lists9 = reservationRepository.findByStatusAndRoomIdAndDeletedAtIsNullAndStartTimeAfter(ReservationStatus.CANCELLED, room.getId(), start);
	    List<Reservation> lists10 = reservationRepository.findByStatusAndRoomIdAndDeletedAtIsNullAndStartTimeAfter(ReservationStatus.CANCELLED, room.getId(), end.plusHours(1));
	    log.info("[STEP 6-1] : DATA : {}", lists9);
	    log.info("[STEP 6-2] : DATA : {}", lists10);
	    assertTrue(lists9.stream().anyMatch(r -> r.getId().equals(cancelled.getId())));
	    assertTrue(lists10.stream().noneMatch(r -> r.getId().equals(cancelled.getId())));
	}
	
}
