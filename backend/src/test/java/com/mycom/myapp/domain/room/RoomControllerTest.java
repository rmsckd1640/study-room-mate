package com.mycom.myapp.domain.room;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.room.controller.RoomController;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.service.RoomService;
import com.mycom.myapp.global.config.SecurityConfig;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.jwt.JwtAccessDeniedHandler;
import com.mycom.myapp.global.jwt.JwtAuthFilter;
import com.mycom.myapp.global.jwt.JwtAuthenticationEntryPoint;
import com.mycom.myapp.global.jwt.JwtProvider;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private RoomService roomService;

	@MockitoBean
	private JwtProvider jwtProvider;

	@MockitoBean
	private JwtAuthFilter jwtAuthFilter;

	@MockitoBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockitoBean
	private JwtAccessDeniedHandler jwtAccessDeniedHandler;

	private RequestPostProcessor withAuth(String username, String... roles) {
		return request -> {
			List<SimpleGrantedAuthority> authorities = List.of(roles).stream().map(SimpleGrantedAuthority::new).toList();
			Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
			return request;
		};
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("room을 조회하면 200과 할인가가 적용된 데이터를 반환한다")
	void getRoom_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, 127500, null);
		given(roomService.getRoom("user1", 1L)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/rooms/{id}", 1L).with(withAuth("user1", "ROLE_USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.data.discountedPrice").value(127500));
	}

	@Test
	@DisplayName("존재하지 않는 room을 조회하면 404를 반환한다")
	void getRoom_실패_존재하지_않음() throws Exception {
		// given
		given(roomService.getRoom("user1", 999L)).willThrow(new RoomNotFoundException("존재하지 않는 room입니다."));

		// when & then
		mockMvc.perform(get("/api/rooms/{id}", 999L).with(withAuth("user1", "ROLE_USER"))).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("존재하지 않는 room입니다."));
	}

	@Test
	@DisplayName("room 목록을 페이징 조회하면 할인가가 적용된 데이터를 반환한다")
	void getRooms_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, 127500, null);
		Page<RoomResponseDto> page = new PageImpl<>(List.of(response));
		given(roomService.getRooms(eq("user1"), any())).willReturn(page);

		// when & then
		mockMvc.perform(get("/api/rooms").with(withAuth("user1", "ROLE_USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].discountedPrice").value(127500));
	}

	@Test
	@DisplayName("이름으로 room을 검색하면 할인가가 적용된 데이터를 반환한다")
	void searchByName_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, 127500, null);
		given(roomService.searchByName("user1", "한강뷰")).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/rooms/search/name").param("name", "한강뷰").with(withAuth("user1", "ROLE_USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].discountedPrice").value(127500));
	}

	@Test
	@DisplayName("최소 수용 인원으로 room을 검색한다")
	void searchByCapacity_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, 127500, null);
		given(roomService.searchByMinCapacity("user1", 4)).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/rooms/search/capacity").param("capacity", "4").with(withAuth("user1", "ROLE_USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].capacity").value(4));
	}

	@Test
	@DisplayName("최대 가격으로 room을 검색한다")
	void searchByPrice_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, 127500, null);
		given(roomService.searchByMaxPrice("user1", 200000)).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/rooms/search/price").param("price", "200000").with(withAuth("user1", "ROLE_USER"))).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].price").value(150000));
	}

	@Test
	@DisplayName("ADMIN 권한으로 room을 생성하면 201을 반환한다")
	void create_성공_ADMIN() throws Exception {
		// given
		RoomCreateRequest request = new RoomCreateRequest("한강뷰 스튜디오", 4, 150000);
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null, null);
		given(roomService.createRoom(any(RoomCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/rooms").with(withAuth("admin1", "ROLE_ADMIN")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.data.name").value("한강뷰 스튜디오"));
	}

	@Test
	@DisplayName("일반 회원이 room 생성을 시도하면 403을 반환한다")
	void create_실패_권한없음() throws Exception {
		// given
		RoomCreateRequest request = new RoomCreateRequest("한강뷰 스튜디오", 4, 150000);

		// when & then
		mockMvc.perform(post("/api/rooms").with(withAuth("user1", "ROLE_USER")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("필수 값이 빠진 채로 room을 생성하면 400을 반환한다")
	void create_실패_검증오류() throws Exception {
		// given
		String invalidRequestJson = """
				{
				    "name": "",
				    "capacity": 4,
				    "price": 150000
				}
				""";

		// when & then
		mockMvc.perform(post("/api/rooms").with(withAuth("admin1", "ROLE_ADMIN")).contentType(MediaType.APPLICATION_JSON).content(invalidRequestJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("ADMIN 권한으로 room을 수정하면 200을 반환한다")
	void update_성공_ADMIN() throws Exception {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("새 이름", 6, 200000);
		RoomResponseDto response = new RoomResponseDto(1L, "새 이름", 6, 200000, null, null);
		given(roomService.updateRoom(eq(1L), any(RoomUpdateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/rooms/{id}", 1L).with(withAuth("admin1", "ROLE_ADMIN")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("새 이름"));
	}

	@Test
	@DisplayName("일반 회원이 room 수정을 시도하면 403을 반환한다")
	void update_실패_권한없음() throws Exception {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("새 이름", 6, 200000);

		// when & then
		mockMvc.perform(patch("/api/rooms/{id}", 1L).with(withAuth("user1", "ROLE_USER")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("ADMIN 권한으로 room을 삭제하면 200을 반환한다")
	void delete_성공_ADMIN() throws Exception {
		// when & then
		mockMvc.perform(delete("/api/rooms/{id}", 1L).with(withAuth("admin1", "ROLE_ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("삭제 성공"));
	}

	@Test
	@DisplayName("일반 회원이 room 삭제를 시도하면 403을 반환한다")
	void delete_실패_권한없음() throws Exception {
		// when & then
		mockMvc.perform(delete("/api/rooms/{id}", 1L).with(withAuth("user1", "ROLE_USER"))).andExpect(status().isForbidden());
	}
}