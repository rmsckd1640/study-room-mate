package com.mycom.myapp.domain.reservation.repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
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

	List<Reservation> findByRoomId(Long roomId);
	Page<Reservation> findByRoomId(Long roomId, Pageable page);
	
	List<Reservation> findByMember_Username(String username);
	Page<Reservation> findByRoomIdAndMember_Username(String username, Long roomId);
	
	List<Reservation> findByStatus(ReservationStatus status);
	Page<Reservation> findByStatus(ReservationStatus status, Pageable page);
	
	Reservation findByIdAndUser_Username(Long reservationId, String username);
	
	List<Reservation> findByStatusAndRoomId(ReservationStatus status, Long roomId);
	Page<Reservation> findByStatusAndRoomId(ReservationStatus status, Long roomId, Pageable page);
	
	List<Reservation> findByStatusAndStartTimeAfter(
			ReservationStatus status, 
			LocalDateTime possibleTime
	);
	Page<Reservation> findByStatusAndStartTimeAfter(
			ReservationStatus status, 
			LocalDateTime possibleTime,
			Pageable page
	);
	
	List<Reservation> findByStatusAndRoomIdAndStartTimeAfter(
			ReservationStatus status, 
			long roomId,
			LocalDateTime possibleTime
	);
	Page<Reservation> findByStatusAndRoomIdAndStartTimeAfter(
			ReservationStatus status, 
			long roomId,
			LocalDateTime possibleTime,
			Pageable page
	);
		
	boolean existsByMember_IdAndRoom_IdAndStatus(Long memberId, Long roomId, ReservationStatus status);
	
}
