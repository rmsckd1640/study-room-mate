package com.mycom.myapp.domain.reservation.service;

import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationAdminService {

	ResultDto<ReservationResponse> confirm(Long reservationId, ReservationStatus status);

	ResultDto<ReservationResponse> reject(Long reservationId, String reason);

}
