package com.mycom.myapp.domain.reservation.service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.entity.Payment;
import com.mycom.myapp.domain.payment.repository.PaymentRepository;
import com.mycom.myapp.domain.payment.service.TossPaymentService;
import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.reservation.repository.ReservationRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.PaymentStatus;
import com.mycom.myapp.global.common.enums.ReservationStatus;
import com.mycom.myapp.global.common.util.SecurityUtils;
import com.mycom.myapp.global.exception.DuplicateReservationException;
import com.mycom.myapp.global.exception.PaymentNotFoundException;
import com.mycom.myapp.global.exception.ReservationNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

	private final ReservationRepository reservationRepository;
	private final TossPaymentService tossPaymentService;
	private final PaymentRepository paymentRepository;
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	private final SecurityUtils securityUtils;
	private final PlatformTransactionManager transactionManager;

	@Transactional
	public ResultDto<ReservationResponse> insert(Long roomId, ReservationInsertRequest request) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

		String username = securityUtils.getCurrentUsername();

		Room room = roomRepository.findByIdForUpdate(roomId).orElseThrow(() -> {
			throw new NoSuchElementException("방을 찾을 수 없습니다");
		});

		validateNoDuplicateReservation(roomId, request);

		Member member = memberRepository.findByUsername(username).orElseThrow(() -> {
			throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
		});

		Reservation reservation = Reservation.builder()
												.room(room)
												.member(member)
												.status(ReservationStatus.PENDING)
												.reservationDate(LocalDate.now())
												.startTime(request.startTime())
												.endTime(request.endTime())
												.build();

		reservation = reservationRepository.save(reservation);

		Payment payment = Payment.builder()
									.reservation(reservation)
									.orderId(UUID.randomUUID().toString())
									.status(PaymentStatus.READY)
									.amount(request.amount())
									.build();

		payment = paymentRepository.save(payment);

		ReservationResponse savedData = reservation.toResponse()
													.withOrderId(payment.getOrderId());

		resultDto.setData(savedData);

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> myList() {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		String username = securityUtils.getCurrentUsername();

		List<ReservationResponse> reservations = reservationRepository.findByMember_UsernameAndDeletedAtIsNull(username)
																		.stream()
																		.map(Reservation::toResponse)
																		.toList();

		resultDto.setData(reservations);

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> myList(Long roomId) {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		String username = securityUtils.getCurrentUsername();

		List<ReservationResponse> reservations = reservationRepository.findByRoomIdAndMember_UsernameAndDeletedAtIsNull(roomId, username)
																		.stream()
																		.map(Reservation::toResponse)
																		.toList();

		resultDto.setData(reservations);

		return resultDto;
	}

	// Toss 환불 호출(외부 I/O)이 DB 트랜잭션을 물고 있으면, 환불은 성공했는데 이후 로컬 저장이
	// 실패했을 때 트랜잭션 전체가 롤백되어 "돈은 나갔는데 예약은 그대로 유효"인 정합성 불일치가 생긴다.
	// 그래서 이 메서드는 의도적으로 @Transactional을 붙이지 않고, 각 단계에서 명시적으로 save한다.
	public ResultDto<ReservationResponse> cancel(Long reservationId, String reason) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

        String username = securityUtils.getCurrentUsername();

        Reservation reservation = reservationRepository.findByIdWithMember(reservationId)
        		.orElseThrow(() -> new ReservationNotFoundException("예약 정보를 찾을 수 없습니다."));

        long minutes = Duration.between(LocalDateTime.now(), reservation.getStartTime()).toMinutes();

        if (!reservation.getMember().getUsername().equals(username)) {
            throw new AccessDeniedException("본인 예약만 취소할 수 있습니다.");
        }

        if (minutes < 90) {
        	throw new IllegalStateException("스터디룸 시작 시간이 임박하여 취소 불가능합니다.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return resultDto;
        }

        if (reservation.getStatus() == ReservationStatus.PAYMENT_DONE
    		|| reservation.getStatus() == ReservationStatus.CONFIRMED) {
        	refundAndCancel(reservation, reason);
        } else {
        	reservation.cancel();
        	reservationRepository.save(reservation);
        }

        resultDto.setMessage("cancled successfuly");

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> statusList(ReservationStatus status) {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		List<ReservationResponse> lists = reservationRepository.findByStatus(status)
																	.stream()
																	.map(Reservation::toResponse)
																	.toList();

		resultDto.setData(lists);

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> statusList(ReservationStatus status, Long roomId) {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		List<ReservationResponse> lists = reservationRepository.findByStatusAndRoomId(status, roomId)
																	.stream()
																	.map(Reservation::toResponse)
																	.toList();

		resultDto.setData(lists);

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> availableSlotList() {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		LocalDateTime time = LocalDateTime.now().plusHours(1).plusMinutes(20);

		List<ReservationResponse> lists = reservationRepository.findByStatusAndDeletedAtIsNullAndStartTimeAfter(ReservationStatus.CANCELLED, time)
																.stream()
																.map(Reservation::toResponse)
																.toList();

		resultDto.setData(lists);

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> availableSlotList(Long roomId) {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		LocalDateTime time = LocalDateTime.now().plusHours(1).plusMinutes(20);

		List<ReservationResponse> lists = reservationRepository.findByStatusAndRoomIdAndDeletedAtIsNullAndStartTimeAfter(ReservationStatus.CANCELLED, roomId, time)
																.stream()
																.map(Reservation::toResponse)
																.toList();

		resultDto.setData(lists);

		return resultDto;
	}

	private void validateNoDuplicateReservation(Long roomId, ReservationInsertRequest request) {
		if (reservationRepository.existsOverlappingReservation(roomId, request.startTime(), request.endTime())) {
			throw new DuplicateReservationException("예약 시간대에 이미 예약이 되어있습니다.");
		}
	}
	
	private void refundAndCancel(Reservation reservation, String reason) {
		Payment payment = paymentRepository.findByReservation_Id(reservation.getId())
				.orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

		// Toss 환불 요청 - 여기서 실패하면 아직 우리 쪽엔 확정된 상태 변경이 없다.
		TossPaymentResponse response = tossPaymentService.cancel(payment.getPaymentKey(), reason, null);
		validateTossCancelResponse(response, payment.getAmount());

		// Toss 환불은 이미 끝났으므로, 이후 로컬 반영이 실패해도 되돌릴 방법이 없다 - 재시도가 아닌 예외 호출.
		// payment/reservation 저장은 원자적으로 묶여야 하므로(둘 중 하나만 저장되는 상황 방지),
		// Toss 호출은 트랜잭션 밖에 둔 채 이 블록만 TransactionTemplate으로 별도 트랜잭션 처리한다.
		try {
			new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
				payment.cancel(reason);
				reservation.cancel();
				paymentRepository.save(payment);
				reservationRepository.save(reservation);
			});
		} catch (Exception e) {
			log.error("[결제 정합성 경고] Toss 환불은 성공했으나 로컬 반영에 실패했습니다 - reservationId: {}, paymentKey: {}. 수동 확인이 필요합니다.",
					reservation.getId(), payment.getPaymentKey(), e);
			markPaymentFailed(payment.getId(), "Toss 환불(예약 취소) 성공 후 로컬 반영 실패: " + e.getMessage());
			throw e;
		}
	}

	// 위 catch에서 로그만 남기면 "Toss 환불은 됐는데 로컬은 그대로"인 사실이 DB 어디에도 남지 않아
	// 관리자가 admin 목록에서 확인할 방법이 없다. 그래서 별도 트랜잭션으로 결제 건에 FAILED 이력을 남긴다.
	// payment는 롤백된 트랜잭션에서 꺼내온 인스턴스라 필드가 오염돼 있을 수 있으므로 id로 새로 조회한다.
	private void markPaymentFailed(Long paymentId, String reason) {
		try {
			new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
				Payment freshPayment = paymentRepository.findById(paymentId)
						.orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));
				freshPayment.fail(reason);
				paymentRepository.save(freshPayment);
			});
		} catch (Exception e) {
			log.error("[결제 정합성 경고] paymentId={} 실패 이력 저장마저 실패했습니다. 수동 확인이 필요합니다.", paymentId, e);
		}
	}

	private void validateTossCancelResponse(TossPaymentResponse response, Long expectedAmount) {
		if (!"CANCELED".equalsIgnoreCase(response.status())) {
			throw new IllegalStateException("결제 취소 실패 : " + response.status());
		}

		List<TossPaymentResponse.Cancel> cancels = response.cancels();
		TossPaymentResponse.Cancel latestCancel = (cancels != null && !cancels.isEmpty()) ? cancels.get(0) : null;

		if (latestCancel == null || !"DONE".equalsIgnoreCase(latestCancel.cancelStatus())) {
			throw new IllegalStateException("결제 취소 실패 : Toss 취소 내역을 확인할 수 없습니다.");
		}

		if (!expectedAmount.equals(latestCancel.cancelAmount())) {
			throw new IllegalStateException("취소 금액이 결제 금액과 일치하지 않습니다.");
		}
	}

}
