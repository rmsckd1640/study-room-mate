package com.mycom.myapp.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.global.common.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

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
	List<Reservation> findByRoomIdAndDeletedAtIsNull(Long roomId);
	
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
	
}
