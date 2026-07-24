package com.mycom.myapp.domain.room.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.review.repository.ReviewRepository;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.entity.Room;
import com.mycom.myapp.domain.room.repository.RoomRepository;
import com.mycom.myapp.domain.wishlist.repository.WishlistRepository;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final MemberRepository memberRepository;
	private final WishlistRepository wishlistRepository;
	private final ReviewRepository reviewRepository;

	@Override
	public RoomResponseDto getRoom(String username, Long roomId) {
		Room room = findRoomOrThrow(roomId);
		Member member = findMemberOrThrow(username);

		boolean wishlisted = wishlistRepository.existsByMember_IdAndRoom_Id(member.getId(), roomId);
		Long wishlistCount = wishlistRepository.countByRoomId(roomId);
		Double averageRating = reviewRepository.findAverageRatingByRoomId(roomId);
		Long reviewCount = reviewRepository.countByRoomId(roomId);

		return RoomResponseDto.from(room, member.getGrade().applyDiscount(room.getPrice()), wishlisted, wishlistCount, averageRating, reviewCount);
	}

	@Override
	public Page<RoomResponseDto> getRooms(String username, Pageable pageable) {
		Member member = findMemberOrThrow(username);
		Page<Room> rooms = roomRepository.findAll(pageable);
		return buildResponseWithBatch(rooms, member);
	}

	@Override
	public List<RoomResponseDto> search(String username, String name, Integer capacity, Integer price) {
		Member member = findMemberOrThrow(username);
		List<Room> rooms = roomRepository.search(name, capacity, price);
		return buildResponseListWithBatch(rooms, member);
	}

	@Override
	public Page<RoomResponseDto> searchWithPaging(String username, String name, Integer capacity, Integer price, Pageable pageable) {
		Member member = findMemberOrThrow(username);
		Page<Room> rooms = roomRepository.search(name, capacity, price, pageable);
		return buildResponseWithBatch(rooms, member);
	}

	@Override
	@Transactional
	public RoomResponseDto createRoom(RoomCreateRequest request) {
		Room saved = roomRepository.save(request.toEntity());
		return RoomResponseDto.from(saved, null);
	}

	@Override
	@Transactional
	public RoomResponseDto updateRoom(Long roomId, RoomUpdateRequest request) {
		Room room = findRoomOrThrow(roomId);
		room.update(request.name(), request.capacity(), request.price());
		return RoomResponseDto.from(room, null);
	}

	@Override
	@Transactional
	public void deleteRoom(Long roomId) {
		Room room = findRoomOrThrow(roomId);
		roomRepository.delete(room);
	}

	private Page<RoomResponseDto> buildResponseWithBatch(Page<Room> rooms, Member member) {
		List<Long> roomIds = rooms.getContent().stream().map(Room::getId).toList();

		Set<Long> wishlistedRoomIds = resolveWishlistedRoomIds(member.getId(), roomIds);
		Map<Long, Long> wishlistCountMap = resolveWishlistCountMap(roomIds);
		Map<Long, Object[]> ratingSummaryMap = resolveRatingSummaryMap(roomIds);

		return rooms.map(room -> toResponseDto(room, member, wishlistedRoomIds, wishlistCountMap, ratingSummaryMap));
	}

	private List<RoomResponseDto> buildResponseListWithBatch(List<Room> rooms, Member member) {
		List<Long> roomIds = rooms.stream().map(Room::getId).toList();

		Set<Long> wishlistedRoomIds = resolveWishlistedRoomIds(member.getId(), roomIds);
		Map<Long, Long> wishlistCountMap = resolveWishlistCountMap(roomIds);
		Map<Long, Object[]> ratingSummaryMap = resolveRatingSummaryMap(roomIds);

		return rooms.stream().map(room -> toResponseDto(room, member, wishlistedRoomIds, wishlistCountMap, ratingSummaryMap)).toList();
	}

	private RoomResponseDto toResponseDto(Room room, Member member, Set<Long> wishlistedRoomIds, Map<Long, Long> wishlistCountMap, Map<Long, Object[]> ratingSummaryMap) {
		Long roomId = room.getId();
		boolean wishlisted = wishlistedRoomIds.contains(roomId);
		Long wishlistCount = wishlistCountMap.getOrDefault(roomId, 0L);

		Object[] ratingRow = ratingSummaryMap.get(roomId);
		Double averageRating = ratingRow != null ? (Double) ratingRow[1] : null;
		Long reviewCount = ratingRow != null ? (Long) ratingRow[2] : 0L;

		return RoomResponseDto.from(room, member.getGrade().applyDiscount(room.getPrice()), wishlisted, wishlistCount, averageRating, reviewCount);
	}

	private Set<Long> resolveWishlistedRoomIds(Long memberId, List<Long> roomIds) {
		if (roomIds.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(wishlistRepository.findWishlistedRoomIds(memberId, roomIds));
	}

	private Map<Long, Long> resolveWishlistCountMap(List<Long> roomIds) {
		if (roomIds.isEmpty()) {
			return Map.of();
		}
		return wishlistRepository.countByRoomIdIn(roomIds).stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
	}

	private Map<Long, Object[]> resolveRatingSummaryMap(List<Long> roomIds) {
		if (roomIds.isEmpty()) {
			return Map.of();
		}
		return reviewRepository.findRatingSummaryByRoomIds(roomIds).stream().collect(Collectors.toMap(row -> (Long) row[0], row -> row));
	}

	private Room findRoomOrThrow(Long roomId) {
		return roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException("존재하지 않는 room입니다. id=" + roomId));
	}

	private Member findMemberOrThrow(String username) {
		return memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
	}
}