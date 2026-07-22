package com.mycom.myapp.domain.wishlist.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.wishlist.entity.Wishlist;

public record WishlistResponseDto(Long id, Long memberId, Long roomId, LocalDateTime createdAt) {
	public static WishlistResponseDto from(Wishlist wishlist) {
		return new WishlistResponseDto(wishlist.getId(), wishlist.getMember().getId(), wishlist.getRoom().getId(), wishlist.getCreatedAt());
	}
}
