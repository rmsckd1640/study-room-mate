package com.mycom.myapp.domain.reservation.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	
	@Query(
	"""
	SELECT	CASE WHEN COUNT(r) > 0 THEN true ELSE false END
	FROM	reservation r
	WHERE	r.roomid = 	:roomId
	AND		r.status <>	'CANCELLED'
	AND		r.deletedAt IS NULL
	AND		r.startTime < :endTime
	AND		r.endTime	> :startTime
	"""
	)
	boolean existsOverlappingReservation(
			@Param("roomId") long roomId,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime
	);
	
}
