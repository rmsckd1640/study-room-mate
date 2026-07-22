package com.mycom.myapp.domain.wishlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.wishlist.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

	List<Wishlist> findByMemberId(Long memberId);

	Optional<Wishlist> findByMember_IdAndRoom_Id(Long memberId, Long roomId);

	boolean existsByMember_IdAndRoom_Id(Long memberId, Long roomId);

	long countByRoomId(Long roomId);
}
