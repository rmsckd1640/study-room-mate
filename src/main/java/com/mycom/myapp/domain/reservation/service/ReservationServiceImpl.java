package com.mycom.myapp.domain.reservation.service;

import java.nio.file.AccessDeniedException;
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
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;
import com.mycom.myapp.global.exception.DuplicateReservationException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	
	private final ReservationRepository reservationRepository;
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	private final SecurityUtils securityUtils;
	
	@Transactional
	public ResultDto<ReservationDto> insert(ReservationDto reservationDto) {
		ResultDto<ReservationDto> resultDto = new ResultDto<>();
		
		try {
			String username = securityUtils.getCurrentUsername();
			
			Room room = roomRepository.findByIdForUpdate(reservationDto.getRoomId()).orElseThrow();
			
			validateNoDuplicateReservation(reservationDto);
			
			Member member = memberRepository.findByUsername(username).orElseThrow();
			
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
	
	public ResultDto<List<ReservationDto>> myList() {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			String username = securityUtils.getCurrentUsername();
			
			List<ReservationDto> reservations = reservationRepository.findByMember_Username(username)
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
	
	public ResultDto<List<ReservationDto>> myList(Long roomId) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			String username = securityUtils.getCurrentUsername();
			
			List<ReservationDto> reservations = reservationRepository.findByRoomIdAndMember_Username(username, roomId)
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
	
	public ResultDto<List<ReservationDto>> list() {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> reservations = reservationRepository.findAll()
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

	public ResultDto<List<ReservationDto>> list(Long roomId) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> reservations = reservationRepository.findByRoomId(roomId)
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
	public ResultDto<ReservationDto> cancle(Long reservationId) {
		ResultDto<ReservationDto> resultDto = new ResultDto<>();
		
		try {
	        String username = securityUtils.getCurrentUsername();
	        
	        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
	        
	        if (!reservation.getMember().getUsername().equals(username)) {
	            throw new AccessDeniedException("본인 예약만 취소할 수 있습니다.");
	        }
	        
	        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
	            return resultDto;
	        }
	        
	        reservation.setStatus(ReservationStatus.CANCELLED);
	        reservation.setDeletedAt(LocalDateTime.now());
	        
	        resultDto.setMessage("cancled successfuly");
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	public ResultDto<List<ReservationDto>> statusList(ReservationStatus status) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> lists = reservationRepository.findByStatus(status)
																	.stream()
																	.map(Reservation::toDto)
																	.toList();
			
			resultDto.setData(lists);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	public ResultDto<List<ReservationDto>> statusList(ReservationStatus status, Long roomId) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			List<ReservationDto> lists = reservationRepository.findByStatusAndRoomId(status, roomId)
																	.stream()
																	.map(Reservation::toDto)
																	.toList();
			
			resultDto.setData(lists);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	@Transactional
	public ResultDto<ReservationDto> confirm(Long reservationId, ReservationStatus status) {
		ResultDto<ReservationDto> resultDto = new ResultDto<>();
		
		try {
			String username = securityUtils.getCurrentUsername();
			
			Reservation reservation = reservationRepository.findByIdAndUser_Username(reservationId, username);
			
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
	
	public ResultDto<List<ReservationDto>> statusCancledList() {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			LocalDateTime time = LocalDateTime.now().plusHours(1).plusMinutes(20);
			
			List<ReservationDto> lists = reservationRepository.findByStatusAndStartTimeAfter(ReservationStatus.CANCELLED, time)
																.stream()
																.map(Reservation::toDto)
																.toList();
			
			resultDto.setData(lists);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			resultDto.setMessage(e.getMessage());
		}
		
		return resultDto;
	}
	
	public ResultDto<List<ReservationDto>> statusCancledList(Long roomId) {
		ResultDto<List<ReservationDto>> resultDto = new ResultDto<>();
		
		try {
			LocalDateTime time = LocalDateTime.now().plusHours(1).plusMinutes(20);
			
			List<ReservationDto> lists = reservationRepository.findByStatusAndRoomIdAndStartTimeAfter(ReservationStatus.CANCELLED, roomId, time)
																.stream()
																.map(Reservation::toDto)
																.toList();
			
			resultDto.setData(lists);
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
