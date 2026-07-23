package com.mycom.myapp.domain.payment.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mycom.myapp.domain.payment.dto.TossConfirmRequest;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.global.exception.TossPaymentException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl implements TossPaymentService {

	private final WebClient tossPaymentWebClient;
	private final PaymentRepository paymentRepository;

	public TossPaymentResponse confirm(TossConfirmRequest request) {
		try {
			Payment payment = paymentRepository.findByOrderId(request.orderId())
					.orElseThrow(() -> new IllegalStateException("결제 정보를 찾을 수 없습니다."));

			if (!payment.getAmount().equals((long) request.amount())) {
				throw new IllegalStateException("결제 금액 != 예약 금액");
			}

			TossPaymentResponse response = tossPaymentWebClient.post()
					.uri("/payments/confirm")
					.bodyValue(request)
					.retrieve()
					.bodyToMono(TossPaymentResponse.class)
					.block();

			if ("DONE".equals(response.status())) {
				payment.complete(response.paymentKey());
				payment.getReservation().paymentDone();
			}

			return response;
		} catch (WebClientResponseException e) {
			throw new TossPaymentException(e.getResponseBodyAsString(), e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public TossPaymentResponse getPaymentKey(String paymentKey) {
		try {
            return tossPaymentWebClient.get()
                    .uri("/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .bodyToMono(TossPaymentResponse.class)
                    .block();
		} catch (WebClientResponseException e) {
			throw new TossPaymentException(e.getResponseBodyAsString(), e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public TossPaymentResponse cancel(String paymentKey, String cancelReason, Long cancelAmount) {
		Map<String, Object> body = cancelAmount != null
				? Map.of("cancelReason", cancelReason, "cancelAmount", cancelAmount)
				: Map.of("cancelReason", cancelReason);

		try {
            return tossPaymentWebClient.post()
                    .uri("/payments/{paymentKey}/cancel", paymentKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TossPaymentResponse.class)
                    .block();
		} catch (WebClientResponseException e) {
			throw new TossPaymentException(e.getResponseBodyAsString(), e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
