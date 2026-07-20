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
	
	List<Reservation> findByMemberId(Long memberId);
	
	@Query(
	"""
	SELECT 	r
	FROM	Reservation r
	WHERE	r.room.id =   :roomId
	AND		r.status <>	  'CANCELLED'
	AND		r.startTime > CURRENT_TIMESTAMP
	"""
	)
	List<Reservation> findByRoomId(Long roomId);
	
	List<Reservation> findByStatus(ReservationStatus status);
	
	List<Reservation> findByStatusAndRoomId(ReservationStatus status, Long roomId);
	
}
