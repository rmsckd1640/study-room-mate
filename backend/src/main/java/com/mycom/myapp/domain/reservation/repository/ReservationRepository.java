package com.mycom.myapp.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	// cancel()이 @Transactional 없이 동작하기 때문에(Toss 환불 호출을 DB 트랜잭션 밖에서 수행하기 위함),
	// 나중에 지연로딩으로 member에 접근하면 LazyInitializationException이 난다. 조회 시점에 함께 가져온다.
	@Query("SELECT r FROM Reservation r JOIN FETCH r.member WHERE r.id = :id")
	Optional<Reservation> findByIdWithMember(@Param("id") Long id);

	@Query(
	"""
	SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
	FROM Reservation r
	WHERE r.room.id = :roomId
	AND   r.status <> 'CANCELLED'
	AND   r.deletedAt IS NULL
	AND   r.startTime < :endTime
	AND   r.endTime   > :startTime
	"""
	)
	boolean existsOverlappingReservation(
			@Param("roomId") long roomId, 
			@Param("startTime") LocalDateTime startTime, 
			@Param("endTime") LocalDateTime endTime
	);
	
	// ADMIN 1. WHERE status = status
	List<Reservation> findByStatus(ReservationStatus status);
	
	// ADMIN 2. WHERE status = status and room_id == roomId
	List<Reservation> findByStatusAndRoomId(ReservationStatus status, Long roomId);

	// USER 1. WHERE room_Id == roomId and deleted_at == null
	Page<Reservation> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable);
	
	// USER 2. WHERE member.username == username and deleted_at == null
	List<Reservation> findByMember_UsernameAndDeletedAtIsNull(String username);	
		
	// USER 3. WHERE room_id = roomId and member.username == username and deleted_at == null
	List<Reservation> findByRoomIdAndMember_UsernameAndDeletedAtIsNull(Long roomId, String username);
	
	// USER 4. WHERE status = status and deleted_at == null and start_time > possibleTime
	List<Reservation> findByStatusAndDeletedAtIsNullAndStartTimeAfter(
			ReservationStatus status, 
			LocalDateTime possibleTime
	);
	
	// USER 5. WHERE status = status and room_id = roomId and deleted_at == null and start_time > possibleTime
	List<Reservation> findByStatusAndRoomIdAndDeletedAtIsNullAndStartTimeAfter(
			ReservationStatus status, 
			long roomId,
			LocalDateTime possibleTime
	);
		
	boolean existsByMember_IdAndRoom_IdAndStatus(Long memberId, Long roomId, ReservationStatus status);

	// 회원 등급 산정용 - 특정 회원의 상태별 예약 개수
	long countByMember_IdAndStatus(Long memberId, ReservationStatus status);

}
