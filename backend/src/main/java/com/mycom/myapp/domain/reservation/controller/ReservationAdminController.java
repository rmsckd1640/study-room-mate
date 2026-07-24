package com.mycom.myapp.domain.reservation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

	private final ReservationAdminService reservationAdminService;

	@Operation(description = "ADMIN : 스터디룸 예약 페이징 조회 (결제 실패 이력 포함)")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping
	public ResponseEntity<ResultDto<Page<ReservationResponse>>> list(
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<ReservationResponse> data = reservationAdminService.list(pageable);

		return ResponseEntity.ok(ResultDto.<Page<ReservationResponse>>builder().data(data).build());
	}

	@Operation(description = "ADMIN : 스터디룸 예약 스터디룸 아이디로 페이징 조회 (결제 실패 이력 포함)")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<ResultDto<Page<ReservationResponse>>> list(
			@PathVariable("id") Long roomId,
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<ReservationResponse> data = reservationAdminService.list(roomId, pageable);

		return ResponseEntity.ok(ResultDto.<Page<ReservationResponse>>builder().data(data).build());
	}
	
	@Operation(description = "ADMIN : 스터디룸 사용자 승인")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/{id}/confirm")
	public ResponseEntity<ResultDto<ReservationResponse>> confirm(
	        @PathVariable("id") Long reservationId,
	        @RequestParam("status") ReservationStatus status) {
	    return ResponseEntity.ok(reservationAdminService.confirm(reservationId, status));
	}

	@Operation(description = "ADMIN : 스터디룸 사용자 거절")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/{id}/reject")
	public ResponseEntity<ResultDto<ReservationResponse>> reject(
	        @PathVariable("id") Long reservationId,
	        @RequestParam(value = "reason", required = false) String reason) {
	    return ResponseEntity.ok(reservationAdminService.reject(reservationId, reason));
	}

}
