package com.mycom.myapp.domain.reservation.dto;

import java.time.LocalDate;
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
	
    private Long id;
    private Long roomId;
    private Long memberId;
    private LocalDate reservationDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
	
}
