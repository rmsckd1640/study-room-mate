package com.mycom.myapp.domain.reservation.service;

import java.util.List;

import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationService {

	ResultDto<ReservationDto> insert(ReservationDto reservationDto);
			
	ResultDto<ReservationDto> cancle(ReservationDto reservationDto);
	
	ResultDto<List<ReservationDto>> list();
	
	ResultDto<List<ReservationDto>> list(long roomId);
	
	ResultDto<List<ReservationDto>> statusList(ReservationStatus status);
	
	ResultDto<List<ReservationDto>> statusList(ReservationStatus status, long roomId);
	
	ResultDto<List<ReservationDto>> statusCancledList();
	
	ResultDto<List<ReservationDto>> statusCancledList(long roomId);
	
	ResultDto<ReservationDto> confirm(long reservationId, ReservationStatus status);
	
}
