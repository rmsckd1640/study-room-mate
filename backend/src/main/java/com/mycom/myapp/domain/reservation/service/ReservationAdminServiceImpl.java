package com.mycom.myapp.domain.reservation.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.payment.service.TossPaymentService;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.exception.ReservationNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationAdminServiceImpl implements ReservationAdminService {

	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;
	private final TossPaymentService tossPaymentService;
	private final PlatformTransactionManager transactionManager;

	@Transactional
	public ResultDto<ReservationResponse> confirm(Long reservationId, ReservationStatus status) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다."));
		
		if (reservation.getStatus() != ReservationStatus.PAYMENT_DONE) {
			throw new IllegalStateException("결제 완료만 예약 확정 가능");
		}
		
		reservation.confirm();
		
		Member member = reservation.getMember();
		long confirmedCount = reservationRepository.countByMember_IdAndStatus(member.getId(), ReservationStatus.CONFIRMED);
		member.updateGrade(confirmedCount);

		return resultDto;
	}

	// confirm()과 달리 이 메서드는 Toss 환불(외부 I/O)을 호출하므로 @Transactional을 붙이지 않는다.
	// 환불이 성공한 뒤 로컬 저장이 실패해도 트랜잭션 롤백으로 "돈은 나갔는데 예약은 그대로"가 되는
	// 상황을 피하기 위해 각 단계를 명시적으로 save하고, 실패 시 정합성 경고만 남긴다.
	public ResultDto<ReservationResponse> reject(Long reservationId, String reason) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != ReservationStatus.PAYMENT_DONE) {
        	throw new IllegalStateException("결제 완료 상태만 거절 가능");
        }

        Payment payment = paymentRepository.findByReservation_Id(reservationId)
        		.orElseThrow(() -> new IllegalStateException("결제 정보를 찾을 수 없습니다."));

        TossPaymentResponse response = tossPaymentService.cancel(
        		payment.getPaymentKey(), reason, null
		);
        validateTossCancelResponse(response, payment.getAmount());

        // payment/reservation 저장이 원자적으로 묶이도록, Toss 호출은 트랜잭션 밖에 둔 채
        // 이 블록만 TransactionTemplate으로 별도 트랜잭션 처리한다.
        try {
        	new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
        		payment.cancel(reason);
        		reservation.reject();
        		paymentRepository.save(payment);
        		reservationRepository.save(reservation);
        	});
        } catch (Exception e) {
        	log.error("[결제 정합성 경고] Toss 환불은 성공했으나 로컬 반영에 실패했습니다 - reservationId: {}, paymentKey: {}. 수동 확인이 필요합니다.",
        			reservationId, payment.getPaymentKey(), e);
        	markPaymentFailed(payment.getId(), "Toss 환불(예약 거절) 성공 후 로컬 반영 실패: " + e.getMessage());
        	throw e;
        }

		return resultDto;
	}

	// 위 catch에서 로그만 남기면 "Toss 환불은 됐는데 로컬은 그대로"인 사실이 DB 어디에도 남지 않아
	// 관리자가 admin 목록에서 확인할 방법이 없다. 그래서 별도 트랜잭션으로 결제 건에 FAILED 이력을 남긴다.
	// payment는 롤백된 트랜잭션에서 꺼내온 인스턴스라 필드가 오염돼 있을 수 있으므로 id로 새로 조회한다.
	private void markPaymentFailed(Long paymentId, String reason) {
		try {
			new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
				Payment freshPayment = paymentRepository.findById(paymentId)
						.orElseThrow(() -> new IllegalStateException("결제 정보를 찾을 수 없습니다."));
				freshPayment.fail(reason);
				paymentRepository.save(freshPayment);
			});
		} catch (Exception e) {
			log.error("[결제 정합성 경고] paymentId={} 실패 이력 저장마저 실패했습니다. 수동 확인이 필요합니다.", paymentId, e);
		}
	}

	@Transactional(readOnly = true)
	public Page<ReservationResponse> list(Pageable pageable) {
		Page<Reservation> reservations = reservationRepository.findAll(pageable);

		return attachPaymentInfo(reservations);
	}

	@Transactional(readOnly = true)
	public Page<ReservationResponse> list(Long roomId, Pageable pageable) {
		Page<Reservation> reservations = reservationRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable);

		return attachPaymentInfo(reservations);
	}

	// 토스 결제 승인 후 로컬 저장 실패로 남은 FAILED 이력을 admin 예약 목록에서 함께 조회할 수 있도록
	// 페이지에 담긴 예약들의 결제 정보를 한 번에 조회해 응답에 채워 넣는다(N+1 방지).
	private Page<ReservationResponse> attachPaymentInfo(Page<Reservation> reservations) {
		List<Long> reservationIds = reservations.getContent()
													.stream()
													.map(Reservation::getId)
													.toList();

		Map<Long, Payment> paymentByReservationId = paymentRepository.findByReservation_IdIn(reservationIds)
																		.stream()
																		.collect(Collectors.toMap(
																				payment -> payment.getReservation().getId(),
																				Function.identity()
																		));

		return reservations.map(reservation -> {
			ReservationResponse response = reservation.toResponse();
			Payment payment = paymentByReservationId.get(reservation.getId());

			return payment != null
					? response.withPaymentStatus(payment.getStatus()).withPaymentFailureReason(payment.getFailureReason())
					: response;
		});
	}

	private void validateTossCancelResponse(TossPaymentResponse response, Long expectedAmount) {
		if (!"CANCELED".equalsIgnoreCase(response.status())) {
			throw new IllegalStateException("결제 취소에 실패 : " + response);
		}

		List<TossPaymentResponse.Cancel> cancels = response.cancels();
		TossPaymentResponse.Cancel latestCancel = (cancels != null && !cancels.isEmpty()) ? cancels.get(0) : null;

		if (latestCancel == null || !"DONE".equalsIgnoreCase(latestCancel.cancelStatus())) {
			throw new IllegalStateException("결제 취소에 실패 : Toss 취소 내역을 확인할 수 없습니다.");
		}

		if (!expectedAmount.equals(latestCancel.cancelAmount())) {
			throw new IllegalStateException("취소 금액이 결제 금액과 일치하지 않습니다.");
		}
	}

}
