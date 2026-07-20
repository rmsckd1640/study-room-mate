package com.mycom.myapp.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.room.dto.RoomDto;
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
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	
	@Transactional
	public ResultDto<ReservationDto> insert(ReservationDto reservationDto) {
		ResultDto<ReservationDto> resultDto = new ResultDto<>();
		
		try {
			Room room = roomRepository.findByIdForUpdate(reservationDto.getRoomId()).orElseThrow();
			
			validateNoDuplicateReservation(reservationDto);
			
			// TODO #2. Security Context 에서 User 추출
			Member member = memberRepository.findById(reservationDto.getMemberId()).orElseThrow();
			
			Reservation reservation = Reservation.builder()
													.room(room)
													.member(member)
													.status(ReservationStatus.PENDING)
													.reservationDate(LocalDate.now())
													.startTime(reservationDto.getStartTime())
													.endTime(reservationDto.getEndTime())
													.build();
			
			reservationRepository.save(reservation);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	public ResultDto<List<ReservationDto>> possibleList(RoomDto roomDto) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> reservations = reservationRepository.findByRoomId(null)
																		.stream()
																		.map(Reservation::toDto)
																		.toList();
			
			resultDto.setData(reservations);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}

	public ResultDto<List<ReservationDto>> list(ReservationDto reservationDto) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			// TODO #1: User Security Context 에서 User 추출해서 reservation repository find
			List<ReservationDto> reservations = reservationRepository.findByMemberId(12L)
																		.stream()
																		.map(Reservation::toDto)
																		.toList();
			
			resultDto.setData(reservations);
		} catch (Exception e) {
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	@Transactional
	public ResultDto<ReservationDto> cancle(ReservationDto reservationDto) {
		ResultDto<ReservationDto> resultDto = new ResultDto<>();
		
		try {
			Reservation reservation = reservationRepository.findById(reservationDto.getId()).orElseThrow();
			
			if (reservation.getStatus() == ReservationStatus.CANCELLED) {
				return resultDto;
			}
			
			reservation.setStatus(ReservationStatus.CANCELLED);
			reservation.setUpdatedAt(LocalDateTime.now());
			
			resultDto.setMessage("cancled successfuly");
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	public ResultDto<List<ReservationDto>> pendingList() {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> pendings = reservationRepository.findByStatus(ReservationStatus.PENDING)
																	.stream()
																	.map(Reservation::toDto)
																	.toList();
					
			
			resultDto.setData(pendings);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	public ResultDto<List<ReservationDto>> pendingList(long roomId) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> pendings = reservationRepository.findByStatusAndRoomId(ReservationStatus.PENDING, roomId)
																	.stream()
																	.map(Reservation::toDto)
																	.toList();
			
			resultDto.setData(pendings);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	@Transactional
	public ResultDto<ReservationDto> confirm(long reservationId, ReservationStatus status) {
		ResultDto<ReservationDto> resultDto = new ResultDto<>();
		
		try {
			Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
			
			switch (status) {
				case CONFIRMED -> {
					reservation.setStatus(status);
					reservation.setUpdatedAt(LocalDateTime.now());
					
					resultDto.setData(reservation.toDto());				
				}
				case CANCELLED -> {
					reservation.setStatus(status);
					reservation.setDeletedAt(LocalDateTime.now());
					
					resultDto.setData(reservation.toDto());				
				}
				default -> {}
			}
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	private void validateNoDuplicateReservation(ReservationDto reservationDto) {
		if (reservationRepository.existsOverlappingReservation(reservationDto.getRoomId(), reservationDto.getStartTime(), reservationDto.getEndTime())) {
			throw new DuplicateReservationException("예약 시간대에 이미 예약이 되어있습니다.");			
		}
	}

	
}
