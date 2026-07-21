package com.mycom.myapp.domain.reservation.service;

import java.util.List;

import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationService {

	ResultDto<ReservationDto> insert(ReservationDto reservationDto);
			
	ResultDto<ReservationDto> cancle(Long reservationId);
	
	ResultDto<List<ReservationDto>> list();
	
	ResultDto<List<ReservationDto>> list(Long roomId);
	
	ResultDto<List<ReservationDto>> myList();
	
	ResultDto<List<ReservationDto>> myList(Long roomId);
	
	ResultDto<List<ReservationDto>> statusList(ReservationStatus status);
	
	ResultDto<List<ReservationDto>> statusList(ReservationStatus status, Long roomId);
	
	ResultDto<List<ReservationDto>> statusCancledList();
	
	ResultDto<List<ReservationDto>> statusCancledList(Long roomId);
	
	ResultDto<ReservationDto> confirm(Long reservationId, ReservationStatus status);
	
}
