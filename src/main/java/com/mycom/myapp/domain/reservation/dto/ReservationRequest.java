package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
	@NotNull(message = "방을 선택해주세요.") Long roomId,
	
	@NotNull(message = "예약 날짜를 입력해주세요.") @Future(message = "예약 시작 시간은 현재보다 미래여야 합니다.") LocalDate reservationDate,
	
	@NotNull(message = "예약 시작 시간을 입력해주세요.") @Future(message = "예약 시작 시간은 현재보다 미래여야 합니다.") LocalDateTime startTime,
	
	@NotNull(message = "예약 종료 시간을 입력해주세요.") @Future(message = "예약 시작 시간은 현재보다 미래여야 합니다.") LocalDateTime endTime	
) {
}
