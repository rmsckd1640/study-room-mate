package com.mycom.myapp.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTest {
	
	@Autowired
	ReservationRepository reservationRepository;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	RoomRepository roomRepository;
	
	@Autowired
	ReservationService reservationService;
	
	@Autowired
	RandomUtils random;

	@Test
	@Order(1)
	public void testInsertTX() throws InterruptedException {
		Room room = roomRepository.save(random.randomRoom());
		
	    int threadCount = 10;
	    List<Member> members = new ArrayList<>();
	    for (int i = 0; i < threadCount; i++) {
	    	members.add(memberRepository.saveAndFlush(random.randomMember()));	    	
	    }
	    
	    LocalDate reservationDate = LocalDate.of(2026, 7, 20);
	    LocalDateTime startTime = LocalDateTime.of(2026, 7, 20, 11, 0);
	    LocalDateTime endTime = LocalDateTime.of(2026, 7, 20, 12, 0);
	    
	    ExecutorService executor = Executors.newFixedThreadPool(4);
	    CountDownLatch latch = new CountDownLatch(threadCount);
	    AtomicInteger successCount = new AtomicInteger();
	    AtomicInteger failCount = new AtomicInteger();
	    
		
		for (int i = 0; i < threadCount; i++) {
			int taskId = i;
			Member member = members.get(i);
			
			executor.submit(() -> {
				try {
					log.info("TASK {} - memberId = {}", taskId, member.getId());
					
					ReservationDto dto = ReservationDto.builder()
							.memberId(member.getId())
							.roomId(room.getId())
							.reservationDate(reservationDate)
							.startTime(startTime)
							.endTime(endTime)
							.build();
	                
	                ResultDto<ReservationDto> result = reservationService.insert(dto);

	                if (result.getMessage() == null) {	                	
	                	successCount.incrementAndGet();
	                } else {
	                	failCount.incrementAndGet();	                	
	                }
				} catch (Exception e) {
					log.warn("TASK {} 실패 : {}", taskId, e.getMessage());
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}
		
	    latch.await(10, TimeUnit.SECONDS);
	    executor.shutdown();

	    log.info("성공: {}, 실패: {}", successCount.get(), failCount.get());
	    assertEquals(1, successCount.get());
	    assertEquals(threadCount - 1, failCount.get());
	}
	
	@Test
	@Order(2)
	public void confirm() {
		
	}

	@Test
	@Order(3)
	public void cancle() {
		
	}
	
	@Test
	@Order(4)
	public void testList() {
		
	}
	
	
}
