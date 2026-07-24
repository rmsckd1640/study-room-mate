package com.mycom.myapp.domain.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.RandomUtils;

@SpringBootTest
public class PaymentRepositoryTest {

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	ReservationRepository reservationRepository;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	RandomUtils random;

	private Reservation saveReservation() {
		Room room = roomRepository.save(random.randomRoom());
		Member member = memberRepository.save(random.randomMember());

		return reservationRepository.save(Reservation.builder()
				.room(room).member(member)
				.status(ReservationStatus.PENDING)
				.reservationDate(LocalDate.of(2026, 10, 1))
				.startTime(LocalDateTime.of(2026, 10, 1, 10, 0))
				.endTime(LocalDateTime.of(2026, 10, 1, 11, 0))
				.build());
	}

	@Test
	public void findByOrderId_주문번호로_조회된다() {
		Reservation reservation = saveReservation();
		String orderId = UUID.randomUUID().toString();

		paymentRepository.save(Payment.builder()
				.reservation(reservation)
				.orderId(orderId)
				.amount(5000L)
				.status(PaymentStatus.READY)
				.build());

		assertTrue(paymentRepository.findByOrderId(orderId).isPresent());
		assertEquals(5000L, paymentRepository.findByOrderId(orderId).orElseThrow().getAmount());
	}

	@Test
	public void findByOrderId_없는주문번호는_empty() {
		assertFalse(paymentRepository.findByOrderId(UUID.randomUUID().toString()).isPresent());
	}

	@Test
	public void findByReservation_Id_예약아이디로_조회된다() {
		Reservation reservation = saveReservation();

		paymentRepository.save(Payment.builder()
				.reservation(reservation)
				.orderId(UUID.randomUUID().toString())
				.amount(5000L)
				.status(PaymentStatus.READY)
				.build());

		assertTrue(paymentRepository.findByReservation_Id(reservation.getId()).isPresent());
	}

	@Test
	public void findByReservation_Id_결제정보없는예약은_empty() {
		Reservation reservation = saveReservation();

		assertFalse(paymentRepository.findByReservation_Id(reservation.getId()).isPresent());
	}

}
