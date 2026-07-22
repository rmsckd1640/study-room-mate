package com.mycom.myapp.domain.payment.service;

import com.mycom.myapp.domain.payment.dto.TossConfirmRequest;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.global.common.dto.ResultDto;

public interface TossPaymentService {

	TossPaymentResponse confirm(TossConfirmRequest request);

	TossPaymentResponse getPaymentKey(String paymentKey);
	
	TossPaymentResponse cancel(String paymentKey, String cancelReason, Long cancelAmount);
}
