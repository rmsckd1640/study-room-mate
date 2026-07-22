package com.mycom.myapp.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.payment.service.TossPaymentService;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.reservation.service.ReservationAdminService;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.RandomUtils;

@SpringBootTest
public class ReservationAdminServiceTest {

	@Autowired
	ReservationRepository reservationRepository;

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	ReservationAdminService reservationAdminService;

	@Autowired
	RandomUtils random;

	@MockitoBean
	TossPaymentService tossPaymentService;

	@BeforeEach
	void resetMocks() {
		reset(tossPaymentService);
	}

	private TossPaymentResponse tossResponseWithStatus(String status) {
		return new TossPaymentResponse(
				null, "test-payment-key", null, null, null, null, null, null,
				null, null, status, null, null, null, null, null, null, null
		);
	}

	private Reservation saveReservation(ReservationStatus status) {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());

		return reservationRepository.save(Reservation.builder()
				.room(room).member(member)
				.status(status)
				.reservationDate(LocalDate.of(2026, 9, 1))
				.startTime(LocalDateTime.of(2026, 9, 1, 10, 0))
				.endTime(LocalDateTime.of(2026, 9, 1, 11, 0))
				.build());
	}

	@Test
	public void confirm_PAYMENT_DONE상태에서_승인된다() {
		Reservation reservation = saveReservation(ReservationStatus.PAYMENT_DONE);

		ResultDto<ReservationResponse> result = reservationAdminService.confirm(reservation.getId(), ReservationStatus.CONFIRMED);

		assertNull(result.getMessage());
		assertEquals(ReservationStatus.CONFIRMED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
	}

	@Test
	public void confirm_PAYMENT_DONE아니면_예외() {
		Reservation reservation = saveReservation(ReservationStatus.PENDING);

		ResultDto<ReservationResponse> result = reservationAdminService.confirm(reservation.getId(), ReservationStatus.CONFIRMED);

		assertNotNull(result.getMessage());
		assertEquals(ReservationStatus.PENDING, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
	}

	@Test
	public void reject_PAYMENT_DONE상태에서_환불후_거절된다() {
		Reservation reservation = saveReservation(ReservationStatus.PAYMENT_DONE);

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

		ResultDto<ReservationResponse> result = reservationAdminService.reject(reservation.getId(), "관리자 거절 사유");

		assertNull(result.getMessage());
		assertEquals(ReservationStatus.REJECTED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());

		Payment payment = paymentRepository.findByReservation_Id(reservation.getId()).orElseThrow();
		assertEquals(PaymentStatus.CANCELED, payment.getStatus());
		verify(tossPaymentService, times(1)).cancel(paymentKey, "관리자 거절 사유", null);
	}

	@Test
	public void reject_Toss환불실패시_거절되지않는다() {
		Reservation reservation = saveReservation(ReservationStatus.PAYMENT_DONE);

		paymentRepository.save(Payment.builder()
				.reservation(reservation)
				.orderId(UUID.randomUUID().toString())
				.paymentKey("test-payment-key-" + UUID.randomUUID())
				.amount(5000L)
				.status(PaymentStatus.DONE)
				.build());

		when(tossPaymentService.cancel(anyString(), anyString(), any()))
				.thenReturn(tossResponseWithStatus("FAILED"));

		ResultDto<ReservationResponse> result = reservationAdminService.reject(reservation.getId(), "관리자 거절 사유");

		assertNotNull(result.getMessage());
		assertEquals(ReservationStatus.PAYMENT_DONE, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());

		Payment payment = paymentRepository.findByReservation_Id(reservation.getId()).orElseThrow();
		assertEquals(PaymentStatus.DONE, payment.getStatus());
	}

	@Test
	public void reject_CONFIRMED상태는_현재_거절불가() {
		Reservation reservation = saveReservation(ReservationStatus.CONFIRMED);

		ResultDto<ReservationResponse> result = reservationAdminService.reject(reservation.getId(), "관리자 거절 사유");

		assertNotNull(result.getMessage());
		assertEquals(ReservationStatus.CONFIRMED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
	}

}
