package com.mycom.myapp.domain.payment.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import com.mycom.myapp.domain.payment.dto.TossConfirmRequest;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;
import com.mycom.myapp.global.exception.PaymentNotFoundException;
import com.mycom.myapp.global.exception.TossPaymentException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl implements TossPaymentService {

	private final SecurityUtils securityUtils;
	private final RestClient tossPaymentRestClient;
	private final PaymentRepository paymentRepository;
	private final ReservationRepository reservationRepository;
	private final PlatformTransactionManager transactionManager;

	public TossPaymentResponse confirm(TossConfirmRequest request) {
		Payment payment = paymentRepository.findByOrderIdWithReservationAndMember(request.orderId())
				.orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

		String username = securityUtils.getCurrentUsername();
		if (!payment.getReservation().getMember().getUsername().equals(username)) {
			throw new AccessDeniedException("본인 결제만 승인할 수 있습니다.");
		}

		if (payment.getStatus() == PaymentStatus.DONE) {
			log.info("이미 승인된 결제에 대한 confirm 재호출 - orderId: {}, paymentKey: {}", request.orderId(), payment.getPaymentKey());
			return getPaymentKey(payment.getPaymentKey());
		}

		if (!payment.getAmount().equals((long) request.amount())) {
			throw new IllegalStateException("결제 금액 != 예약 금액");
		}

		TossPaymentResponse response = requestConfirm(request);

		if ("DONE".equals(response.status())) {
			completePayment(payment, response);
		}

		return response;
	}

	public TossPaymentResponse getPaymentKey(String paymentKey) {
		return tossPaymentRestClient.get()
				.uri("/payments/{paymentKey}", paymentKey)
				.retrieve()
				.body(TossPaymentResponse.class);
	}

	public TossPaymentResponse cancel(String paymentKey, String cancelReason, Long cancelAmount) {
		Map<String, Object> body = cancelAmount != null
				? Map.of("cancelReason", cancelReason, "cancelAmount", cancelAmount)
				: Map.of("cancelReason", cancelReason);

		return tossPaymentRestClient.post()
				.uri("/payments/{paymentKey}/cancel", paymentKey)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.body(body)
				.retrieve()
				.body(TossPaymentResponse.class);
	}

	private TossPaymentResponse requestConfirm(TossConfirmRequest request) {
		return tossPaymentRestClient.post()
				.uri("/payments/confirm")
				.body(request)
				.retrieve()
				.body(TossPaymentResponse.class);
	}

	private void completePayment(Payment payment, TossPaymentResponse response) {
		try {
			// payment/reservation 저장이 원자적으로 묶이도록 TransactionTemplate으로 별도 트랜잭션 처리한다.
			// (Toss 승인 호출은 이미 끝난 뒤라 이 블록엔 외부 I/O가 없다.)
			new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
				payment.complete(response.paymentKey());
				payment.getReservation().paymentDone();

				paymentRepository.save(payment);
				reservationRepository.save(payment.getReservation());
			});
		} catch (Exception e) {
			log.error("결제 승인 후처리 실패, Toss 결제 자동 취소를 시도합니다 - orderId: {}, paymentKey: {}",
					payment.getOrderId(), response.paymentKey(), e);
			compensateByCancel(response.paymentKey());
			markPaymentFailed(payment.getId(), e.getMessage());
			throw new TossPaymentException("결제 후처리 중 오류가 발생하여 결제가 취소되었습니다.",
					HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void compensateByCancel(String paymentKey) {
		try {
			cancel(paymentKey, "결제 후처리 실패로 인한 자동 취소", null);
		} catch (Exception cancelEx) {
			log.error("[결제 정합성 경고] paymentKey={} 자동 취소마저 실패했습니다. 수동 확인이 필요합니다.", paymentKey, cancelEx);
		}
	}

	// 위 completePayment()의 트랜잭션이 롤백되면 실패했다는 사실 자체가 DB에 남지 않아 관리자가 조회할 방법이 없다.
	// 그래서 실패 표시만 별도 트랜잭션으로 다시 저장한다. payment는 롤백된 트랜잭션에서 꺼내온 인스턴스라
	// complete() 호출로 오염된 필드가 남아 있을 수 있으므로, id로 새로 조회해 깨끗한 상태에서 fail()을 적용한다.
	private void markPaymentFailed(Long paymentId, String reason) {
		try {
			new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
				Payment freshPayment = paymentRepository.findById(paymentId)
						.orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));
				freshPayment.fail(reason);
				paymentRepository.save(freshPayment);
			});
		} catch (Exception e) {
			log.error("[결제 정합성 경고] paymentId={} 실패 이력 저장마저 실패했습니다. 수동 확인이 필요합니다.", paymentId, e);
		}
	}

}
