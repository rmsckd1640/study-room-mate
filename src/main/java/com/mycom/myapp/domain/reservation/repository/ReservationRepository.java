package com.mycom.myapp.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	
}
