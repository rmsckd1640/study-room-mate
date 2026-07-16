package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.global.common.enums.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
	
	private int roomId;
	private LocalDateTime reservationDate;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private ReservationStatus status;
	
}
