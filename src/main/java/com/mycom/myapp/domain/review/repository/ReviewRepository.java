package com.mycom.myapp.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByMemberId(Long memberId);
	List<Review> findByRoomId(Long roomId);
	Page<Review> findByRoomIdOrderByRatingDesc(Long roomId, Pageable pageable);

	long countByRoomId(Long roomId);
	boolean existsByMember_IdAndRoom_Id(Long memberId, Long roomId);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId")
	Double findAverageRatingByRoomId(@Param("roomId") Long roomId);
}
