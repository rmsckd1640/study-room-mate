package com.mycom.myapp.domain.payment.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.payment.dto.TossConfirmRequest;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse.Failure;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;
import com.mycom.myapp.global.exception.TossPaymentException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl implements TossPaymentService {

	private final SecurityUtils securityUtils;
	private final WebClient tossPaymentWebClient;
	private final PaymentRepository paymentRepository;
	private final ReservationRepository reservationRepository;
	private final ObjectMapper objectMapper;

	public TossPaymentResponse confirm(TossConfirmRequest request) {
		Payment payment = paymentRepository.findByOrderId(request.orderId())
				.orElseThrow(() -> new IllegalStateException("결제 정보를 찾을 수 없습니다."));

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
		try {
            return tossPaymentWebClient.get()
                    .uri("/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .bodyToMono(TossPaymentResponse.class)
                    .block();
		} catch (WebClientResponseException e) {
			throw toTossPaymentException(e);
		}
	}

	public TossPaymentResponse cancel(String paymentKey, String cancelReason, Long cancelAmount) {
		Map<String, Object> body = cancelAmount != null
				? Map.of("cancelReason", cancelReason, "cancelAmount", cancelAmount)
				: Map.of("cancelReason", cancelReason);

		try {
            return tossPaymentWebClient.post()
                    .uri("/payments/{paymentKey}/cancel", paymentKey)
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TossPaymentResponse.class)
                    .block();
		} catch (WebClientResponseException e) {
			throw toTossPaymentException(e);
		}
	}

	private TossPaymentResponse requestConfirm(TossConfirmRequest request) {
		try {
			return tossPaymentWebClient.post()
					.uri("/payments/confirm")
					.bodyValue(request)
					.retrieve()
					.bodyToMono(TossPaymentResponse.class)
					.block();
		} catch (WebClientResponseException e) {
			log.error("Toss 결제 승인 실패 - orderId: {}, body: {}", request.orderId(), e.getResponseBodyAsString());
			throw toTossPaymentException(e);
		}
	}

	private void completePayment(Payment payment, TossPaymentResponse response) {
		try {
			payment.complete(response.paymentKey());
			payment.getReservation().paymentDone();

			paymentRepository.save(payment);
			reservationRepository.save(payment.getReservation());
		} catch (Exception e) {
			log.error("결제 승인 후처리 실패, Toss 결제 자동 취소를 시도합니다 - orderId: {}, paymentKey: {}",
					payment.getOrderId(), response.paymentKey(), e);
			compensateByCancel(response.paymentKey());
			throw new TossPaymentException("결제 후처리 중 오류가 발생하여 결제가 취소되었습니다.",
					HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void compensateByCancel(String paymentKey) {
		try {
			cancel(paymentKey, "결제 후처리 실패로 인한 자동 취소", null);
		} catch (Exception cancelEx) {
			log.error("[결제 ERROR] paymentKey={} 자동 취소 실패, 수동 확인이 필요합니다.", paymentKey, cancelEx);
		}
	}

	private TossPaymentException toTossPaymentException(WebClientResponseException e) {
		Failure failure = parseFailure(e.getResponseBodyAsString());
		HttpStatusCode tossStatus = e.getStatusCode();
		HttpStatus clientStatus = tossStatus.is4xxClientError() ? HttpStatus.BAD_REQUEST : HttpStatus.BAD_GATEWAY;
		return new TossPaymentException(failure.message(), tossStatus, clientStatus);
	}

	private Failure parseFailure(String body) {
		try {
			return objectMapper.readValue(body, Failure.class);
		} catch (Exception e) {
			return new Failure("UNKNOWN", "결제 처리 중 오류가 발생했습니다.");
		}
	}

}
