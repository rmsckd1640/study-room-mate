package com.mycom.myapp.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.payment.service.TossPaymentService;
import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class ReservationServiceTest {

	@Autowired
	ReservationRepository reservationRepository;

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	ReservationService reservationService;

	@Autowired
	RandomUtils random;

	@MockitoBean
	TossPaymentService tossPaymentService;

	@BeforeEach
	void resetMocks() {
		reset(tossPaymentService);
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private void loginAs(Member member) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						member.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
				)
		);
	}

	private TossPaymentResponse tossResponseWithStatus(String status) {
		return new TossPaymentResponse(
				null, "test-payment-key", null, null, null, null, null, null,
				null, null, status, null, null, null, null, null, null, null
		);
	}

	@Test
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

					loginAs(member);

					ReservationInsertRequest request = new ReservationInsertRequest(
							reservationDate, startTime, endTime, 1000L
					);

	                ResultDto<ReservationResponse> result = reservationService.insert(room.getId(), request);

	                if (result.getMessage() == null) {
	                	successCount.incrementAndGet();
	                } else {
	                	failCount.incrementAndGet();
	                }
				} catch (Exception e) {
					log.warn("TASK {} 실패 : {}", taskId, e.getMessage());
					failCount.incrementAndGet();
				} finally {
					SecurityContextHolder.clearContext();
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
	public void insert_예약과_결제를_함께_생성한다() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());
		loginAs(member);

		ReservationInsertRequest request = new ReservationInsertRequest(
				LocalDate.of(2026, 8, 1),
				LocalDateTime.of(2026, 8, 1, 10, 0),
				LocalDateTime.of(2026, 8, 1, 11, 0),
				5000L
		);

		ResultDto<ReservationResponse> result = reservationService.insert(room.getId(), request);

		assertNull(result.getMessage());

		ReservationResponse response = result.getData();
		assertEquals(ReservationStatus.PENDING, response.status());
		assertNotNull(response.orderId());
		assertEquals(5000L, response.amount());

		Payment payment = paymentRepository.findByOrderId(response.orderId()).orElseThrow();
		assertEquals(PaymentStatus.READY, payment.getStatus());
		assertEquals(5000L, payment.getAmount());
	}

	@Test
	public void insert_겹치는_시간대면_실패한다() {
		Room room = roomRepository.save(random.randomRoom());
		Member member1 = memberRepository.save(random.randomMember());
		Member member2 = memberRepository.save(random.randomMember());

		LocalDate date = LocalDate.of(2026, 8, 2);
		LocalDateTime start = LocalDateTime.of(2026, 8, 2, 14, 0);
		LocalDateTime end = LocalDateTime.of(2026, 8, 2, 15, 0);

		loginAs(member1);
		ResultDto<ReservationResponse> first = reservationService.insert(
				room.getId(), new ReservationInsertRequest(date, start, end, 5000L)
		);
		assertNull(first.getMessage());

		loginAs(member2);
		ResultDto<ReservationResponse> second = reservationService.insert(
				room.getId(), new ReservationInsertRequest(date, start, end, 5000L)
		);
		assertNotNull(second.getMessage());
	}

	@Test
	public void cancel_본인이_아니면_거절된다() {
		Room room = roomRepository.save(random.randomRoom());
		Member owner = memberRepository.save(random.randomMember());
		Member stranger = memberRepository.save(random.randomMember());

		Reservation reservation = reservationRepository.save(Reservation.builder()
				.room(room).member(owner)
				.status(ReservationStatus.PENDING)
				.reservationDate(LocalDate.of(2026, 8, 3))
				.startTime(LocalDateTime.of(2026, 8, 3, 10, 0))
				.endTime(LocalDateTime.of(2026, 8, 3, 11, 0))
				.build());

		loginAs(stranger);
		ResultDto<ReservationResponse> result = reservationService.cancel(reservation.getId(), "취소 사유");

		assertNotNull(result.getMessage());
		assertEquals(ReservationStatus.PENDING, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
		verify(tossPaymentService, never()).cancel(anyString(), anyString(), any());
	}

	@Test
	public void cancel_PENDING상태는_Toss호출없이_취소된다() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());

		Reservation reservation = reservationRepository.save(Reservation.builder()
				.room(room).member(member)
				.status(ReservationStatus.PENDING)
				.reservationDate(LocalDate.of(2026, 8, 4))
				.startTime(LocalDateTime.of(2026, 8, 4, 10, 0))
				.endTime(LocalDateTime.of(2026, 8, 4, 11, 0))
				.build());

		loginAs(member);
		ResultDto<ReservationResponse> result = reservationService.cancel(reservation.getId(), "단순 변심");

		assertEquals("cancled successfuly", result.getMessage());
		assertEquals(ReservationStatus.CANCELLED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
		verify(tossPaymentService, never()).cancel(anyString(), anyString(), any());
	}

	@Test
	public void cancel_PAYMENT_DONE상태는_Toss환불후_취소된다() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());

		Reservation reservation = reservationRepository.save(Reservation.builder()
				.room(room).member(member)
				.status(ReservationStatus.PAYMENT_DONE)
				.reservationDate(LocalDate.of(2026, 8, 5))
				.startTime(LocalDateTime.of(2026, 8, 5, 10, 0))
				.endTime(LocalDateTime.of(2026, 8, 5, 11, 0))
				.build());

		String paymentKey = "test-payment-key-" + UUID.randomUUID();
		paymentRepository.save(Payment.builder()
				.reservation(reservation)
				.orderId(UUID.randomUUID().toString())
				.paymentKey(paymentKey)
				.amount(5000L)
				.status(PaymentStatus.DONE)
				.build());

		when(tossPaymentService.cancel(anyString(), anyString(), any()))
				.thenReturn(tossResponseWithStatus("CANCELED"));

		loginAs(member);
		ResultDto<ReservationResponse> result = reservationService.cancel(reservation.getId(), "환불 요청");

		assertEquals("cancled successfuly", result.getMessage());
		assertEquals(ReservationStatus.CANCELLED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());

		Payment payment = paymentRepository.findByReservation_Id(reservation.getId()).orElseThrow();
		assertEquals(PaymentStatus.CANCELED, payment.getStatus());

		verify(tossPaymentService, times(1)).cancel(paymentKey, "환불 요청", null);
	}

	@Test
	public void cancel_Toss환불실패시_그대로유지된다() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());

		Reservation reservation = reservationRepository.save(Reservation.builder()
				.room(room).member(member)
				.status(ReservationStatus.PAYMENT_DONE)
				.reservationDate(LocalDate.of(2026, 8, 6))
				.startTime(LocalDateTime.of(2026, 8, 6, 10, 0))
				.endTime(LocalDateTime.of(2026, 8, 6, 11, 0))
				.build());

		paymentRepository.save(Payment.builder()
				.reservation(reservation)
				.orderId(UUID.randomUUID().toString())
				.paymentKey("test-payment-key-" + UUID.randomUUID())
				.amount(5000L)
				.status(PaymentStatus.DONE)
				.build());

		when(tossPaymentService.cancel(anyString(), anyString(), any()))
				.thenReturn(tossResponseWithStatus("FAILED"));

		loginAs(member);
		ResultDto<ReservationResponse> result = reservationService.cancel(reservation.getId(), "환불 요청");

		assertNotNull(result.getMessage());
		assertEquals(ReservationStatus.PAYMENT_DONE, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());

		Payment payment = paymentRepository.findByReservation_Id(reservation.getId()).orElseThrow();
		assertEquals(PaymentStatus.DONE, payment.getStatus());
	}

	@Test
	public void myList_내_예약만_조회된다() {
		Room room = roomRepository.save(random.randomRoom());
		Member me = memberRepository.save(random.randomMember());
		Member other = memberRepository.save(random.randomMember());

		Reservation mine = reservationRepository.save(Reservation.builder()
				.room(room).member(me)
				.status(ReservationStatus.PENDING)
				.reservationDate(LocalDate.of(2026, 8, 7))
				.startTime(LocalDateTime.of(2026, 8, 7, 9, 0))
				.endTime(LocalDateTime.of(2026, 8, 7, 10, 0))
				.build());

		reservationRepository.save(Reservation.builder()
				.room(room).member(other)
				.status(ReservationStatus.PENDING)
				.reservationDate(LocalDate.of(2026, 8, 7))
				.startTime(LocalDateTime.of(2026, 8, 7, 11, 0))
				.endTime(LocalDateTime.of(2026, 8, 7, 12, 0))
				.build());

		loginAs(me);
		ResultDto<List<ReservationResponse>> result = reservationService.myList();

		assertNull(result.getMessage());
		assertTrue(result.getData().stream().anyMatch(r -> r.id().equals(mine.getId())));
		assertFalse(result.getData().isEmpty());
	}

	@Test
	public void statusList_상태로_필터링된다() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());

		Reservation confirmed = reservationRepository.save(Reservation.builder()
				.room(room).member(member)
				.status(ReservationStatus.CONFIRMED)
				.reservationDate(LocalDate.of(2026, 8, 8))
				.startTime(LocalDateTime.of(2026, 8, 8, 9, 0))
				.endTime(LocalDateTime.of(2026, 8, 8, 10, 0))
				.build());

		ResultDto<List<ReservationResponse>> result = reservationService.statusList(ReservationStatus.CONFIRMED);

		assertNull(result.getMessage());
		assertTrue(result.getData().stream().anyMatch(r -> r.id().equals(confirmed.getId())));
	}

}
