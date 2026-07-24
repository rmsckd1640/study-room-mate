package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.enums.ReservationStatus;

import lombok.Builder;
import lombok.With;

@With
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservationResponse(

	Long id,
	Long roomId,
	String orderId,
	LocalDate reservationDate,
	LocalDateTime startTime,
	LocalDateTime endTime,
	ReservationStatus status,
	// ADMIN 목록 조회 시에만 채워짐 - 토스 결제 승인 후 로컬 저장 실패로 인한 FAILED 이력을 함께 조회하기 위함
	PaymentStatus paymentStatus,
	String paymentFailureReason

) {
}
