package com.mycom.myapp.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.room.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
