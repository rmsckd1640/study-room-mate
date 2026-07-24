package com.mycom.myapp.domain.room.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.room.entity.Room;

import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findByNameContaining(String name);

	List<Room> findByCapacityGreaterThanEqual(Integer capacity);

	List<Room> findByPriceLessThanEqual(Integer price);

	Page<Room> findByCapacityGreaterThanEqual(Integer capacity, Pageable pageable);

	Page<Room> findByPriceLessThanEqual(Integer price, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT r FROM Room r WHERE r.id = :roomId")
	Optional<Room> findByIdForUpdate(@Param("roomId") Long roomId);
}
