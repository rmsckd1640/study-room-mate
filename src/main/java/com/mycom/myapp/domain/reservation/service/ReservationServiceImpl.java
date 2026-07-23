package com.mycom.myapp.domain.reservation.service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

	private final ReservationRepository reservationRepository;
	private final TossPaymentService tossPaymentService;
	private final PaymentRepository paymentRepository;
	private final MemberRepository memberRepository;
	private final RoomRepository roomRepository;
	private final SecurityUtils securityUtils;

	@Transactional
	public ResultDto<ReservationResponse> insert(Long roomId, ReservationInsertRequest request) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

		String username = securityUtils.getCurrentUsername();

		Room room = roomRepository.findByIdForUpdate(roomId).orElseThrow();

		validateNoDuplicateReservation(roomId, request);

		Member member = memberRepository.findByUsername(username).orElseThrow();

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

	public ResultDto<List<ReservationResponse>> list() {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		List<ReservationResponse> reservations = reservationRepository.findAll()
																		.stream()
																		.map(Reservation::toResponse)
																		.toList();

		resultDto.setData(reservations);

		return resultDto;
	}

	public ResultDto<List<ReservationResponse>> list(Long roomId) {
		ResultDto<List<ReservationResponse>> resultDto = new ResultDto<>();

		List<ReservationResponse> reservations = reservationRepository.findByRoomIdAndDeletedAtIsNull(roomId)
																		.stream()
																		.map(Reservation::toResponse)
																		.toList();

		resultDto.setData(reservations);

		return resultDto;
	}

	@Transactional
	public ResultDto<ReservationResponse> cancel(Long reservationId, String reason) {
		ResultDto<ReservationResponse> resultDto = new ResultDto<>();

        String username = securityUtils.getCurrentUsername();
        
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

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
        	Payment payment = paymentRepository.findByReservation_Id(reservationId)
        			.orElseThrow(() -> new IllegalStateException("결제 정보를 찾을 수 없습니다."));

        	TossPaymentResponse response = tossPaymentService.cancel(
        			payment.getPaymentKey(), reason, null
			);

        	if (!"CANCELED".equalsIgnoreCase(response.status())) {
        		throw new IllegalStateException("결제 취소 실패 : " + response.status());
        	}

        	payment.cancel(reason);
        }

        reservation.cancel();

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

}
