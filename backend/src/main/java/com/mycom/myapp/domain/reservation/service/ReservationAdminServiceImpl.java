package com.mycom.myapp.domain.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationAdminServiceImpl implements ReservationAdminService {

	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;
	private final TossPaymentService tossPaymentService;

	@Transactional
	public ResultDto<ReservationResponse> confirm(Long reservationId, ReservationStatus status) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

		try {
	        Reservation reservation = reservationRepository.findById(reservationId)
	                .orElseThrow(() -> new IllegalStateException("예약을 찾을 수 없습니다."));

	        if (reservation.getStatus() != ReservationStatus.PAYMENT_DONE) {
	        	throw new IllegalStateException("결제 완료만 예약 확정 가능");
	        }

			reservation.confirm();

			Member member = reservation.getMember();
			long confirmedCount = reservationRepository.countByMember_IdAndStatus(member.getId(), ReservationStatus.CONFIRMED);
			member.updateGrade(confirmedCount);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}

		return resultDto;
	}

	@Transactional
	public ResultDto<ReservationResponse> reject(Long reservationId, String reason) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

		try {
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

	        if (!"CANCELED".equalsIgnoreCase(response.status())) {
	        	throw new IllegalStateException("결제 취소에 실패 : " + response);
	        }

	        payment.cancel(reason);
			reservation.reject();
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}

		return resultDto;
	}

}
