package com.mycom.myapp.domain.room.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.room.entity.Room;

public interface RoomRepositoryCustom {
	List<Room> search(String name, Integer capacity, Integer price);
	Page<Room> search(String name, Integer capacity, Integer price, Pageable pageable);
}