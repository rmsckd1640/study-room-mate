package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mycom.myapp.global.common.util.ValidReservationTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

@ValidReservationTime
public record ReservationInsertRequest(
		
	@NotNull(message = "예약 날짜를 입력해주세요.") @FutureOrPresent(message = "예약 날짜는 오늘 이후여야 합니다.") LocalDate reservationDate,
	
	@NotNull(message = "예약 시작 시간을 입력해주세요.") @Future(message = "예약 시작 시간은 현재보다 후여야 합니다.") LocalDateTime startTime,
	
	@NotNull(message = "예약 종료 시간을 입력해주세요.") @Future(message = "예약 시작 시간은 현재보다 후여야 합니다.") LocalDateTime endTime,
	
	@NotNull(message = "금액을 입력해주세요.") Long amount
	
) {
}
