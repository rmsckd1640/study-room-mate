package com.mycom.myapp.domain.payment.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(

	String version,
	String paymentKey,
	String type,
	String orderId,
	String orderName,
	String mId,
	String currency,
	String method,
	Long totalAmount,
	Long balanceAmount,
	String status,
	String requestedAt,
	String approvedAt,
	Boolean useEscrow,
	String lastTransactionKey,
	Card card,
	VirtualAccount virtualAccount,
	Failure failure,
	List<Cancel> cancels

) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Card(
		String issuerCode,
		String acquirerCode,
		String number,
		Integer installmentPlanMonths,
		String approveNo,
		String cardType,
		String ownerType,
		String acquireStatus
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record VirtualAccount(
		String accountType,
		String accountNumber,
		String bankCode,
		String customerName,
		String dueDate,
		String refundStatus,
		Boolean expired
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Failure(
		String code,
		String message
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Cancel(
		String cancelReason,
		String canceledAt,
		Long cancelAmount,
		String cancelStatus
	) {
	}

}
