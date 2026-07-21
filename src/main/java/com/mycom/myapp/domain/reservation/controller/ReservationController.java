package com.mycom.myapp.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.reservation.dto.ReservationDto;
import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

	private final ReservationService reservationService;
	private final SecurityUtils securityUtils;
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/admin")
	public ResponseEntity<ResultDto<List<ReservationDto>>> list() {
		ResultDto<List<ReservationDto>> list = reservationService.list();
		
		return securityUtils.isAdmin() ? ResponseEntity.ok(list) : ResponseEntity.badRequest().build();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/admin/{id}")
	public ResponseEntity<ResultDto<List<ReservationDto>>> list(@PathVariable Long roomId) {
		ResultDto<List<ReservationDto>> list = reservationService.list(roomId);
		
		return securityUtils.isAdmin() ? ResponseEntity.ok(list) : ResponseEntity.badRequest().build();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/admin")
	public ResponseEntity<ResultDto<ReservationDto>> confirm(@PathVariable Long reservationId, ReservationStatus status) {
		ResultDto<ReservationDto> result = reservationService.confirm(reservationId, status);
		
		return securityUtils.isAdmin() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().build();
	}
	
	@PostMapping("/{id}")
	public ResponseEntity<ResultDto<ReservationDto>> insert(@PathVariable Long roomId, ReservationInsertRequest request) {
		ReservationDto dto = ReservationDto.builder()
				.id(roomId)
				.startTime(request.startTime())
				.endTime(request.endTime())
				.reservationDate(request.reservationDate())
				.build();
		
		return ResponseEntity.ok(reservationService.insert(dto));
	}
	
	@GetMapping("/my")
	public ResponseEntity<ResultDto<List<ReservationDto>>> myList() {
		return ResponseEntity.ok(reservationService.myList());
	}
	
	@GetMapping("/my/{id}")
	public ResponseEntity<ResultDto<List<ReservationDto>>> myList(@PathVariable("id") Long roomId) {
		return ResponseEntity.ok(reservationService.myList(roomId));
	}
	
	@PostMapping("/{reservationId}/cancel")
	public ResponseEntity<ResultDto<ReservationDto>> cancle(@PathVariable("reservationId") Long reservationId) {
	    return ResponseEntity.ok(reservationService.cancle(reservationId));
	}
	
}
