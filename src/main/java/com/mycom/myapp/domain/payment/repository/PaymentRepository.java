package com.mycom.myapp.domain.payment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderId(String orderId);

	Optional<Payment> findByReservation_Id(Long reservationId);

	// ADMIN : 예약 목록 조회 시 결제 정보(결제 실패 이력 포함)를 함께 채워 넣기 위한 배치 조회
	List<Payment> findByReservation_IdIn(Collection<Long> reservationIds);

}
