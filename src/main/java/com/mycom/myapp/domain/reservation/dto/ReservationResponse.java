package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
	Long amount,
	LocalDate reservationDate,
	LocalDateTime startTime,
	LocalDateTime endTime,
	ReservationStatus status

) {
}
