package com.mycom.myapp.domain.payment.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.global.common.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", nullable = false, unique = true)
	private Reservation reservation;

	@Column(nullable = false, unique = true, name = "order_id")
	private String orderId;

	@Column(name = "payment_key", unique = true)
	private String paymentKey;

	@Column(nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private PaymentStatus status;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "canceled_at")
	private LocalDateTime canceledAt;

	@Column(name = "cancel_reason")
	private String cancelReason;

	@Column(name = "failure_reason")
	private String failureReason;

	@Column(name = "failed_at")
	private LocalDateTime failedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public void complete(String paymentKey) {
		this.paymentKey = paymentKey;
		this.status = PaymentStatus.DONE;
		this.approvedAt = LocalDateTime.now();
	}

	public void cancel(String reason) {
		this.status = PaymentStatus.CANCELED;
		this.canceledAt = LocalDateTime.now();
		this.cancelReason = reason;
	}

	// 토스 결제는 승인됐지만 로컬 저장 트랜잭션이 실패해 보상 취소(compensateByCancel)로 환불한 경우,
	// 관리자가 예약 목록에서 실패 이력을 조회할 수 있도록 남겨두는 상태.
	public void fail(String reason) {
		this.status = PaymentStatus.FAILED;
		this.failureReason = reason;
		this.failedAt = LocalDateTime.now();
	}

}
