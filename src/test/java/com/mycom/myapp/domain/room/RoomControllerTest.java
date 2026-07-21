package com.mycom.myapp.domain.room;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.room.controller.RoomController;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.domain.room.service.RoomService;
import com.mycom.myapp.global.exception.RoomNotFoundException;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private RoomService roomService;

	@Test
	@DisplayName("존재하는 room을 조회하면 200과 데이터를 반환한다")
	void getRoom_성공() throws Exception {
		// given
		RoomResponseDto response = new RoomResponseDto(1L, "한강뷰 스튜디오", 4, 150000, null);
		given(roomService.getRoom(1L)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/rooms/{id}", 1L)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("조회 성공")).andExpect(jsonPath("$.data.name").value("한강뷰 스튜디오")).andExpect(jsonPath("$.data.location").value("서울 성동구"));
	}

	@Test
	@DisplayName("존재하지 않는 room을 조회하면 404와 에러 메시지를 반환한다")
	void getRoom_실패_존재하지_않음() throws Exception {
		// given
		given(roomService.getRoom(999L)).willThrow(new RoomNotFoundException("존재하지 않는 room입니다."));

		// when & then
		mockMvc.perform(get("/api/rooms/{id}", 999L)).andDo(print()).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("존재하지 않는 room입니다.")).andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	@DisplayName("필수 값이 빠진 채로 room을 생성하면 400을 반환한다")
	void create_실패_검증오류() throws Exception {
		// given - name이 빈 문자열인 잘못된 요청
		String invalidRequestJson = """
				{
				    "name": "",
				    "location": "서울 성동구",
				    "capacity": 4,
				    "price": 150000
				}
				""";

		// when & then
		mockMvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(invalidRequestJson)).andExpect(status().isBadRequest());
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
}
