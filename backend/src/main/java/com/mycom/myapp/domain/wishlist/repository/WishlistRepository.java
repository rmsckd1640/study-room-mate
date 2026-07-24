package com.mycom.myapp.domain.wishlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.domain.wishlist.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

	List<Wishlist> findByMemberId(Long memberId);

	Optional<Wishlist> findByMember_IdAndRoom_Id(Long memberId, Long roomId);

	boolean existsByMember_IdAndRoom_Id(Long memberId, Long roomId);

	long countByRoomId(Long roomId);

	@Query("SELECT w.room.id FROM Wishlist w WHERE w.member.id = :memberId AND w.room.id IN :roomIds")
	List<Long> findWishlistedRoomIds(@Param("memberId") Long memberId, @Param("roomIds") List<Long> roomIds);

	@Query("SELECT w.room.id, COUNT(w) FROM Wishlist w WHERE w.room.id IN :roomIds GROUP BY w.room.id")
	List<Object[]> countByRoomIdIn(@Param("roomIds") List<Long> roomIds);
}
