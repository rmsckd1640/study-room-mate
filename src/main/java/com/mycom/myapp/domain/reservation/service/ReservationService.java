package com.mycom.myapp.domain.reservation.service;

import java.util.List;

import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.room.dto.RoomDto;
import com.mycom.myapp.global.common.dto.ResultDto;

public interface ReservationService {

	ResultDto<ReservationDto> reservationInsert(ReservationDto reservationDto);
	
	ResultDto<List<ReservationDto>> reservationPossibleList(RoomDto roomDto);
	
	ResultDto<List<ReservationDto>> reservationList(ReservationDto reservationDto);
	
	ResultDto<ReservationDto> reservationCancle(ReservationDto reservationDto);
	
}
