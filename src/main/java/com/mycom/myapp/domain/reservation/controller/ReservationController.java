package com.mycom.myapp.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

	private final ReservationService reservationService;
	private final SecurityUtils securityUtils;
	
	@Operation(description = "ADMIN : 스터디룸 예약 조회")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping("/admin")
	public ResponseEntity<ResultDto<List<ReservationDto>>> list() {
		ResultDto<List<ReservationDto>> list = reservationService.list();
		
		return securityUtils.isAdmin() ? ResponseEntity.ok(list) : ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "ADMIN : 스터디룸 예약 스터디룸 아이디 조회")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping("/admin/{id}")
	public ResponseEntity<ResultDto<List<ReservationDto>>> list(@PathVariable("id") Long roomId) {
		ResultDto<List<ReservationDto>> list = reservationService.list(roomId);
		
		return securityUtils.isAdmin() ? ResponseEntity.ok(list) : ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "ADMIN : 스터디룸 사용자 상태 변경")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/admin/{id}")
	public ResponseEntity<ResultDto<ReservationDto>> confirm(
	        @PathVariable("id") Long reservationId,
	        @RequestParam ReservationStatus status) {
	    return ResponseEntity.ok(reservationService.confirm(reservationId, status));
	}
	
	@Operation(description = "USER : 스터디룸 사용자 예약")
	@PostMapping("/{id}")
	public ResponseEntity<ResultDto<ReservationDto>> insert(
			@PathVariable("id") Long roomId, 
			@RequestBody @Valid ReservationInsertRequest request
	) {
		ReservationDto dto = ReservationDto.builder()
				.roomId(roomId)
				.startTime(request.startTime())
				.endTime(request.endTime())
				.reservationDate(request.reservationDate())
				.build();
		
		return ResponseEntity.ok(reservationService.insert(dto));
	}
	
	@Operation(description = "USER : 예약한 스터디룸 전체 조회")
	@GetMapping("/my")
	public ResponseEntity<ResultDto<List<ReservationDto>>> myList() {
		return ResponseEntity.ok(reservationService.myList());
	}
	
	@Operation(description = "USER : 예약한 스터디룸 아이디 조회")
	@GetMapping("/my/{id}")
	public ResponseEntity<ResultDto<List<ReservationDto>>> myList(@PathVariable("id") Long roomId) {
		return ResponseEntity.ok(reservationService.myList(roomId));
	}
	
	@Operation(description = "USER : 상태별 스터디룸 조회")
	@GetMapping("/status")
	public ResponseEntity<ResultDto<List<ReservationDto>>> statusList(@RequestParam("status") ReservationStatus status) {		
		return status != ReservationStatus.CANCELLED 
				? ResponseEntity.ok(reservationService.statusList(status)) 
				: ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "USER : 상태별 스터디룸 아이디로 조회")
	@GetMapping("/status/{id}")
	public ResponseEntity<ResultDto<List<ReservationDto>>> statusList(
			@RequestParam("status") ReservationStatus status, 
			@PathVariable("id") Long roomId
	) {		
		return status != ReservationStatus.CANCELLED 
				? ResponseEntity.ok(reservationService.statusList(status, roomId)) 
				: ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "USER : 취소로 인해 예약 가능해진 시간대 조회")
	@GetMapping("/available")
	public ResponseEntity<ResultDto<List<ReservationDto>>> availableRoom() {
		return ResponseEntity.ok(reservationService.availableSlotList());
	}
	
	@Operation(description = "USER : 취소로 인해 예약 가능해진 스터디룸 시간대 조회")
	@GetMapping("/available/{id}")
	public ResponseEntity<ResultDto<List<ReservationDto>>> availableRoom(@PathVariable("id") Long roomId) {
		return ResponseEntity.ok(reservationService.availableSlotList(roomId));
	}
	
	@Operation(description = "USER : 예약 취소")
	@PostMapping("/{id}/cancel")
	public ResponseEntity<ResultDto<ReservationDto>> cancle(@PathVariable("id") Long reservationId) {
	    return ResponseEntity.ok(reservationService.cancle(reservationId));
	}
	
}
