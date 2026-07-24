package com.mycom.myapp.domain.reservation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationAdminService {

	ResultDto<ReservationResponse> confirm(Long reservationId, ReservationStatus status);

	ResultDto<ReservationResponse> reject(Long reservationId, String reason);

	// 결제 실패(FAILED) 이력을 포함해 예약 목록을 페이지 단위로 조회한다.
	Page<ReservationResponse> list(Pageable pageable);

	Page<ReservationResponse> list(Long roomId, Pageable pageable);

}
