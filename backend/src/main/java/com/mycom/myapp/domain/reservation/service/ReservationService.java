package com.mycom.myapp.domain.reservation.service;

import java.util.List;

import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationService {

	ResultDto<ReservationResponse> insert(Long roomId, ReservationInsertRequest request);

	ResultDto<ReservationResponse> cancel(Long reservationId, String reason);

	ResultDto<List<ReservationResponse>> myList();

	ResultDto<List<ReservationResponse>> myList(Long roomId);

	ResultDto<List<ReservationResponse>> statusList(ReservationStatus status);

	ResultDto<List<ReservationResponse>> statusList(ReservationStatus status, Long roomId);

	ResultDto<List<ReservationResponse>> availableSlotList();

	ResultDto<List<ReservationResponse>> availableSlotList(Long roomId);

}
