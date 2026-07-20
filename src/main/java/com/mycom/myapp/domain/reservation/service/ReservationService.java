package com.mycom.myapp.domain.reservation.service;

import java.util.List;

import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationService {

	ResultDto<ReservationDto> insert(ReservationDto reservationDto);
	
	ResultDto<List<ReservationDto>> possibleList(RoomDto roomDto);
	
	ResultDto<List<ReservationDto>> list(ReservationDto reservationDto);
	
	ResultDto<ReservationDto> cancle(ReservationDto reservationDto);
	
	ResultDto<List<ReservationDto>> pendingList();
	
	ResultDto<List<ReservationDto>> pendingList(long roomId);
	
	ResultDto<ReservationDto> confirm(long reservationId, ReservationStatus status);
	
}
