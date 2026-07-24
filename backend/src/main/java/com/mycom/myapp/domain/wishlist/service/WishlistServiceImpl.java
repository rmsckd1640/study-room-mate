package com.mycom.myapp.domain.wishlist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.wishlist.dto.WishlistCreateRequest;
import com.mycom.myapp.domain.wishlist.dto.WishlistResponseDto;
import com.mycom.myapp.domain.wishlist.entity.Wishlist;
import com.mycom.myapp.domain.wishlist.repository.WishlistRepository;
import com.mycom.myapp.global.exception.DuplicateWishlistException;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;
import com.mycom.myapp.global.exception.WishlistNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

	private final WishlistRepository wishlistRepository;

	private final MemberRepository memberRepository;

	private final RoomRepository roomRepository;

	private void validateNotDuplicate(Long memberId, Long roomId) {
		if (wishlistRepository.existsByMember_IdAndRoom_Id(memberId, roomId)) {
			throw new DuplicateWishlistException("이미 이 방을 즐겨찾기 했습니다.");
		}
	}

	@Override
	@Transactional
	public WishlistResponseDto createWishlist(String username, WishlistCreateRequest request) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new RoomNotFoundException("존재하지 않는 room입니다."));
		validateNotDuplicate(member.getId(), room.getId());
		Wishlist wishlist = Wishlist.builder().member(member).room(room).build();
		Wishlist saved = wishlistRepository.save(wishlist);
		return WishlistResponseDto.from(saved);
	}

	@Override
	@Transactional
	public void deleteWishlist(String username, Long roomId) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		Wishlist wishlist = wishlistRepository.findByMember_IdAndRoom_Id(member.getId(), roomId).orElseThrow(() -> new WishlistNotFoundException("즐겨찾기한 기록이 없습니다."));
		wishlistRepository.delete(wishlist);
	}

	@Override
	public List<WishlistResponseDto> getWishlistsByMember(String username) {
		Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
		return wishlistRepository.findByMemberId(member.getId()).stream().map(WishlistResponseDto::from).toList();
	}

	@Override
	public long countByRoomId(Long roomId) {
		return wishlistRepository.countByRoomId(roomId);
	}
}
