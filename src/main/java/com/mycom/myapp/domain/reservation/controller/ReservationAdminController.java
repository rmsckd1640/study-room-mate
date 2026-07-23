package com.mycom.myapp.domain.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.service.ReservationAdminService;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservation")
public class ReservationAdminController {

	private final ReservationService reservationService;
	private final ReservationAdminService reservationAdminService;
	private final SecurityUtils securityUtils;

	@Operation(description = "ADMIN : 스터디룸 예약 조회")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping
	public ResponseEntity<ResultDto<List<ReservationResponse>>> list() {
		ResultDto<List<ReservationResponse>> list = reservationService.list();

		return securityUtils.isAdmin() ? ResponseEntity.ok(list) : ResponseEntity.badRequest().build();
	}

	@Operation(description = "ADMIN : 스터디룸 예약 스터디룸 아이디 조회")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<ResultDto<List<ReservationResponse>>> list(@PathVariable("id") Long roomId) {
		ResultDto<List<ReservationResponse>> list = reservationService.list(roomId);

		return securityUtils.isAdmin() ? ResponseEntity.ok(list) : ResponseEntity.badRequest().build();
	}
	
	@Operation(description = "ADMIN : 스터디룸 사용자 승인")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/{id}/confirm")
	public ResponseEntity<ResultDto<ReservationResponse>> confirm(
	        @PathVariable("id") Long reservationId,
	        @RequestParam ReservationStatus status) {
	    return ResponseEntity.ok(reservationAdminService.confirm(reservationId, status));
	}

	@Operation(description = "ADMIN : 스터디룸 사용자 거절")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/{id}/reject")
	public ResponseEntity<ResultDto<ReservationResponse>> reject(
	        @PathVariable("id") Long reservationId,
	        @RequestParam(required = false) String reason) {
	    return ResponseEntity.ok(reservationAdminService.reject(reservationId, reason));
	}

}
