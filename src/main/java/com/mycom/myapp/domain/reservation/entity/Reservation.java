package com.mycom.myapp.domain.reservation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.mycom.myapp.domain.reservation.dto.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation")
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
//	@ManyToOne(fetchType = FetchType.EAGER)
//	private List<User> userId;
	
//	@ManyToOne(fetchType = FetchType.EAGER)
//	private List<Room> roomId;
	
	@Column(nullable = false, name = "reservation_date")
	private LocalDateTime reservationDate;
	
	@Column(nullable = false, name = "start_time")
	private LocalDateTime startTime;
	
	@Column(nullable = false, name = "end_time")
	private LocalDateTime endTime;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ReservationStatus status;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
}
