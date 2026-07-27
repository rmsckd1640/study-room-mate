package com.mycom.myapp.domain.payment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderId(String orderId);

	Optional<Payment> findByReservation_Id(Long reservationId);

	// ADMIN : 예약 목록 조회 시 결제 정보(결제 실패 이력 포함)를 함께 채워 넣기 위한 배치 조회
	List<Payment> findByReservation_IdIn(Collection<Long> reservationIds);

	// confirm()은 Toss 승인 API 호출(외부 I/O)을 트랜잭션 밖에서 수행해야 하므로 @Transactional로
	// 감쌀 수 없다. 대신 본인 확인(payment.getReservation().getMember())에 필요한 지연로딩 프록시를
	// 세션 없이도 접근할 수 있도록 조회 시점에 JOIN FETCH로 함께 가져온다.
	@Query("SELECT p FROM Payment p JOIN FETCH p.reservation r JOIN FETCH r.member WHERE p.orderId = :orderId")
	Optional<Payment> findByOrderIdWithReservationAndMember(@Param("orderId") String orderId);

}
