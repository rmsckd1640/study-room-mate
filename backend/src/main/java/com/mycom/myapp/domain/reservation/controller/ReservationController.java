package com.mycom.myapp.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

	private final ReservationService reservationService;
	
	@Operation(description = "USER : 스터디룸 사용자 예약")
	@PostMapping("/{id}")
	public ResponseEntity<ResultDto<ReservationResponse>> insert(
			@PathVariable("id") Long roomId, 
			@RequestBody @Valid ReservationInsertRequest request
	) {
		return ResponseEntity.ok(reservationService.insert(roomId, request));
	}
	
	@Operation(description = "USER : 예약한 스터디룸 전체 조회")
	@GetMapping("/my")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> myList() {
		return ResponseEntity.ok(reservationService.myList());
	}
	
	@Operation(description = "USER : 예약한 스터디룸 아이디 조회")
	@GetMapping("/my/{id}")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> myList(@PathVariable("id") Long roomId) {
		return ResponseEntity.ok(reservationService.myList(roomId));
	}
	
	@Operation(description = "USER : 상태별 스터디룸 조회")
	@GetMapping("/status")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> statusList(@RequestParam("status") ReservationStatus status) {		
		return status != ReservationStatus.CANCELLED 
				? ResponseEntity.ok(reservationService.statusList(status)) 
				: ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "USER : 상태별 스터디룸 아이디로 조회")
	@GetMapping("/status/{id}")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> statusList(
			@RequestParam("status") ReservationStatus status, 
			@PathVariable("id") Long roomId
	) {		
		return status != ReservationStatus.CANCELLED 
				? ResponseEntity.ok(reservationService.statusList(status, roomId)) 
				: ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "USER : 취소로 인해 예약 가능해진 시간대 조회")
	@GetMapping("/available")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> availableRoom() {
		return ResponseEntity.ok(reservationService.availableSlotList());
	}
	
	@Operation(description = "USER : 취소로 인해 예약 가능해진 스터디룸 시간대 조회")
	@GetMapping("/available/{id}")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> availableRoom(@PathVariable("id") Long roomId) {
		return ResponseEntity.ok(reservationService.availableSlotList(roomId));
	}
	
	@Operation(description = "USER : 예약 취소")
	@PostMapping("/{id}/cancel")
	public ResponseEntity<ResultDto<ReservationResponse>> cancle(@PathVariable("id") Long reservationId, @RequestParam(value = "reason", required = false) String reason) {
	    return ResponseEntity.ok(reservationService.cancel(reservationId, reason));
	}
	
}
