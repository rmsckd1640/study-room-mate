package com.mycom.myapp.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record TossConfirmRequest(

	@NotNull String paymentKey,
	@NotNull String orderId,
	@NotNull Integer amount

) {
}
