package com.mycom.myapp.domain.room.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.room.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findByName(String name);
	List<Room> findByLocation(String location);
	List<Room> findByCapacityGreaterThanEqual(int capacity);
	List<Room> findByPriceLessThanEqual(int price);
}
