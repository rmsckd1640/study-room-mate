package com.mycom.myapp.domain.reservation.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.exception.DuplicateReservationException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	
	private final ReservationRepository reservationRepository;
	private final RoomRepository roomRepository;
	
	@Transactional
	ResultDto<String> reservationInsert(ReservationDto reservationDto) {
		ResultDto<String> resultDto = new ResultDto();
		
		try {
			validateNoDuplicateReservation(reservationDto);
			
			Room room = roomRepository.findById(reservationDto.getRoomId()).orElseThrow();
			Member member = Member.builder().email("test@test.com").name("엄주호").password("1234").role(MemberRole.USER).build();
			
			Reservation reservation = Reservation.builder()
													.room(room)
													.member(member)
													.status(ReservationStatus.PENDING)
													.reservationDate(LocalDate.now())
													.startTime(reservationDto.getStartDate())
													.endTime(reservationDto.getEndDate())
													.build();
			
			reservationRepository.save(reservation);
			
			resultDto.setResult("success");
			resultDto.setStatus(HttpStatus.OK);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setResult("fail");
			resultDto.setStatus(HttpStatus.BAD_REQUEST);
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}

	ResultDto<String> reservationList(ReservationDto reservationDto) {
		ResultDto<String> resultDto = new ResultDto();
		
		try {
			// TODO #1: User Security Context 에서 User 추출해서 reservation repository find
			reservationRepository.findById(null);
		} catch (Exception e) {
			e.printStackTrace();
			resultDto.setResult("fail");
		}
		
		return resultDto;
	}
	
	@Transactional
	ResultDto<String> reservationDelete(ReservationDto reservationDto) {
		ResultDto<String> resultDto = new ResultDto();
		
		try {
			
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setResult("fail");
		}
		
		return resultDto;
	}
	
	private void validateNoDuplicateReservation(ReservationDto reservationDto) {
		if (reservationRepository.existsOverlappingReservation(reservationDto.getRoomId(), reservationDto.getStartDate(), reservationDto.getEndDate())) {
			throw new DuplicateReservationException("예약 시간대에 이미 예약이 되어있습니다.");			
		}
	}
	
}
