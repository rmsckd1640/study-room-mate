package com.mycom.myapp.domain.wishlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.wishlist.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
	List<Wishlist> findByMemberId(Long memberId);
	boolean existsByMember_IdAndRoom_Id(Long memberId, Long roomId);
	long countByMemberId(Long memberId);
}
