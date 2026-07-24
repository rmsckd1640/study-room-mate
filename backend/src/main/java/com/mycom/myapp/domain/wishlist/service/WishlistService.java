package com.mycom.myapp.domain.wishlist.service;

import java.util.List;

import com.mycom.myapp.domain.wishlist.dto.WishlistCreateRequest;
import com.mycom.myapp.domain.wishlist.dto.WishlistResponseDto;

public interface WishlistService {

	WishlistResponseDto createWishlist(String username, WishlistCreateRequest request);

	void deleteWishlist(String username, Long roomId);

	List<WishlistResponseDto> getWishlistsByMember(String username);

	long countByRoomId(Long roomId);
}