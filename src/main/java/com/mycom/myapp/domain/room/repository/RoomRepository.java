package com.mycom.myapp.domain.room.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.room.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findByNameContaining(String name);

	List<Room> findByLocationContaining(String location);

	List<Room> findByCapacityGreaterThanEqual(Integer capacity);

	List<Room> findByPriceLessThanEqual(Integer price);

	Page<Room> findByCapacityGreaterThanEqual(Integer capacity, Pageable pageable);

	Page<Room> findByPriceLessThanEqual(Integer price, Pageable pageable);
}
