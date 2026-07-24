package com.mycom.myapp.domain.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.payment.dto.TossConfirmRequest;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.service.TossPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

	private final TossPaymentService tossPaymentService;

	@Operation(description = "토스 사용자 결제 승인 요청")
	@PostMapping("/confirm")
	public ResponseEntity<TossPaymentResponse> confirm(@RequestBody @Valid TossConfirmRequest request) {
		TossPaymentResponse response = tossPaymentService.confirm(request);
		
		return ResponseEntity.ok(response);
	}
	
}
