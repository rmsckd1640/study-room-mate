package com.mycom.myapp.domain.wishlist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.wishlist.controller.WishlistController;
import com.mycom.myapp.domain.wishlist.dto.WishlistCreateRequest;
import com.mycom.myapp.domain.wishlist.dto.WishlistResponseDto;
import com.mycom.myapp.domain.wishlist.service.WishlistService;
import com.mycom.myapp.global.common.util.SecurityUtils;
import com.mycom.myapp.global.exception.DuplicateWishlistException;
import com.mycom.myapp.global.exception.WishlistNotFoundException;
import com.mycom.myapp.global.jwt.JwtProvider;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private WishlistService wishlistService;

	@MockitoBean
	private JwtProvider jwtProvider;

	@MockitoBean
	private SecurityUtils securityUtils; // 추가

	private RequestPostProcessor withAuth(String username) {
		return request -> {
			Authentication auth = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
			SecurityContextHolder.getContext().setAuthentication(auth);
			return request;
		};
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("찜을 추가하면 201과 데이터를 반환한다")
	void create_성공() throws Exception {
		// given
		given(securityUtils.getCurrentUsername()).willReturn("user1");
		WishlistCreateRequest request = new WishlistCreateRequest(5L);
		WishlistResponseDto response = new WishlistResponseDto(1L, 10L, 5L, null);
		given(wishlistService.createWishlist(eq("user1"), any(WishlistCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/wishlists").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.message").value("즐겨찾기 성공")).andExpect(jsonPath("$.data.roomId").value(5L));
	}

	@Test
	@DisplayName("roomId가 없으면 400을 반환한다")
	void create_실패_검증오류() throws Exception {
		// given
		given(securityUtils.getCurrentUsername()).willReturn("user1");
		String invalidJson = "{}";

		// when & then
		mockMvc.perform(post("/api/wishlists").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("이미 찜한 방이면 409를 반환한다")
	void create_실패_중복() throws Exception {
		// given
		given(securityUtils.getCurrentUsername()).willReturn("user1");
		WishlistCreateRequest request = new WishlistCreateRequest(5L);
		given(wishlistService.createWishlist(eq("user1"), any(WishlistCreateRequest.class))).willThrow(new DuplicateWishlistException("이미 이 방을 즐겨찾기 했습니다."));

		// when & then
		mockMvc.perform(post("/api/wishlists").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("이미 이 방을 즐겨찾기 했습니다."));
	}

	@Test
	@DisplayName("찜을 삭제하면 200을 반환한다")
	void delete_성공() throws Exception {
		// given
		given(securityUtils.getCurrentUsername()).willReturn("user1");

		// when & then
		mockMvc.perform(delete("/api/wishlists/{id}", 5L).with(withAuth("user1"))).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("즐겨찾기 삭제 성공"));
	}

	@Test
	@DisplayName("찜한 기록이 없으면 삭제 시 404를 반환한다")
	void delete_실패_기록없음() throws Exception {
		// given
		given(securityUtils.getCurrentUsername()).willReturn("user1");
		org.mockito.BDDMockito.willThrow(new WishlistNotFoundException("찜한 기록이 없습니다.")).given(wishlistService).deleteWishlist(eq("user1"), eq(5L));

		// when & then
		mockMvc.perform(delete("/api/wishlists/{id}", 5L).with(withAuth("user1"))).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("찜한 기록이 없습니다."));
	}

	@Test
	@DisplayName("내 찜 목록을 조회한다")
	void getWishlists_성공() throws Exception {
		// given
		given(securityUtils.getCurrentUsername()).willReturn("user1");
		WishlistResponseDto response = new WishlistResponseDto(1L, 10L, 5L, null);
		given(wishlistService.getWishlistsByMember("user1")).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/wishlists").with(withAuth("user1"))).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].roomId").value(5L));
	}

	@Test
	@DisplayName("room이 찜된 횟수를 조회한다")
	void countByRoom_성공() throws Exception {
		// given - securityUtils를 안 쓰는 API라 stubbing 불필요
		given(wishlistService.countByRoomId(5L)).willReturn(3L);

		// when & then
		mockMvc.perform(get("/api/wishlists/room/{roomId}", 5L)).andExpect(status().isOk()).andExpect(jsonPath("$.data").value(3));
	}
}