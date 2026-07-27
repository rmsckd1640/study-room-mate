package com.mycom.myapp.domain.payment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTimeoutScheduler {

	private static final long TIMEOUT_MINUTES = 10;

	private final PaymentRepository paymentRepository;

	@Scheduled(fixedDelay = 60_000)
	@Transactional
	public void cancelExpiredPendingReservations() {
		LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

		List<Payment> expiredPayments = paymentRepository.findExpiredReadyPayments(expiredBefore);

		if (expiredPayments.isEmpty()) {
			return;
		}

		for (Payment payment : expiredPayments) {
			payment.expire("결제 시간 초과(" + TIMEOUT_MINUTES + "분)로 자동 취소");
			payment.getReservation().cancel();
		}

		log.info("결제 시간 초과로 예약 {}건을 자동 취소했습니다.", expiredPayments.size());
	}

}
