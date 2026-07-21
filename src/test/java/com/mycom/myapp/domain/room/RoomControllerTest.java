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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.room.controller.RoomController;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.dto.RoomUpdateRequest;
import com.mycom.myapp.domain.room.service.RoomService;
import com.mycom.myapp.global.exception.RoomNotFoundException;
import com.mycom.myapp.global.jwt.JwtProvider;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private RoomService roomService;

	@MockitoBean
	private JwtProvider jwtProvider;

	@Test
	@DisplayName("이름으로 room을 검색한다")
	void searchByName_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		given(roomService.searchByName("한강뷰")).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/rooms/search/name").param("name", "한강뷰")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].name").value("한강뷰 스튜디오"));
	}

	@Test
	@DisplayName("최소 수용 인원으로 room을 검색한다")
	void searchByCapacity_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		given(roomService.searchByMinCapacity(4)).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/rooms/search/capacity").param("capacity", "4")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].capacity").value(4));
	}

	@Test
	@DisplayName("최대 가격으로 room을 검색한다")
	void searchByPrice_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		given(roomService.searchByMaxPrice(200000)).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/rooms/search/price").param("price", "200000")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].price").value(150000));
	}

	@Test
	@DisplayName("존재하는 room을 조회하면 200과 데이터를 반환한다")
	void getRoom_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		given(roomService.getRoom(1L)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/rooms/{id}", 1L)).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("조회 성공")).andExpect(jsonPath("$.data.name").value("한강뷰 스튜디오"));
	}

	@Test
	@DisplayName("존재하지 않는 room을 조회하면 404를 반환한다")
	void getRoom_실패_존재하지_않음() throws Exception {
		// given
		given(roomService.getRoom(999L)).willThrow(new RoomNotFoundException("존재하지 않는 room입니다."));

		// when & then
		mockMvc.perform(get("/api/rooms/{id}", 999L)).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("존재하지 않는 room입니다."));
	}

	@Test
	@DisplayName("room 목록을 페이징 조회한다")
	void getRooms_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		Page<RoomResponseDto> page = new PageImpl<>(List.of(response));
		given(roomService.getRooms(any())).willReturn(page);

		// when & then
		mockMvc.perform(get("/api/rooms")).andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].name").value("한강뷰 스튜디오"));
	}

	@Test
	@DisplayName("정상적인 요청으로 room을 생성하면 201을 반환한다")
	void create_성공() throws Exception {
		// given
		RoomCreateRequest request = new RoomCreateRequest("한강뷰 스튜디오", 4, 150000);
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		given(roomService.createRoom(any(RoomCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.data.name").value("한강뷰 스튜디오"));
	}

	@Test
	@DisplayName("필수 값이 빠진 채로 room을 생성하면 400을 반환한다")
	void create_실패_검증오류() throws Exception {
		// given - name이 빈 문자열인 잘못된 요청
		String invalidRequestJson = """
				{
				    "name": "",
				    "capacity": 4,
				    "price": 150000
				}
				""";

		// when & then
		mockMvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(invalidRequestJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("room을 수정하면 200과 변경된 데이터를 반환한다")
	void update_성공() throws Exception {
		// given
		RoomUpdateRequest request = new RoomUpdateRequest("새 이름", 6, 200000);
		RoomResponseDto response = new RoomResponseDto(1L, "새 이름", 6, 200000, null);
		given(roomService.updateRoom(eq(1L), any(RoomUpdateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/rooms/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("새 이름"));
	}

	@Test
	@DisplayName("room을 삭제하면 200을 반환한다")
	void delete_성공() throws Exception {
		// when & then
		mockMvc.perform(delete("/api/rooms/{id}", 1L)).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("삭제 성공"));
	}
}