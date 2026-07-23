package com.mycom.myapp.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.jwt.JwtProvider;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MvpTest {
		
	@Autowired
	JwtProvider provider;
	
	@Autowired
	JsonMapper objectMapper;
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/*
	 * ADMIN -> create room -> 승인하고 -> 회원 로그인하고 -> 방 목록 조회 -> 예약
	*/
	@Test
	@Order(1)
	@Transactional
	public void testMVC1() throws Exception {

		// STEP 0 : 테스트용 계정 직접 삽입
		memberRepository.save(Member.builder()
				.username("Admin")
				.password(passwordEncoder.encode("password"))
				.email("admin@test.com")
				.name("admin")
				.role(MemberRole.ADMIN)
				.build());

		memberRepository.save(Member.builder()
				.username("User")
				.password(passwordEncoder.encode("password"))
				.email("user@test.com")
				.name("user")
				.role(MemberRole.USER)
				.build());

		LoginRequest req1 = new LoginRequest("Admin", "password");
		LoginRequest req2 = new LoginRequest("User", "password");
		RoomCreateRequest rcreq = new RoomCreateRequest("8조", 3, 10000);

		// STEP 1 : 관리자 계정으로 로그인
		log.debug("[STEP 1 : 관리자 로그인]");
		MvcResult admin = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req1)))
				.andExpect(status().isOk())
				.andReturn();

		LoginResponse responseAdmin = extractData(admin, LoginResponse.class);
		String accessTokenAdmin = responseAdmin.accessToken();

		// STEP 2 : 관리자 계정으로 스터디룸 생성
		log.debug("[STEP 2 : 스터디룸 생성]");
		MvcResult result1 = mockMvc.perform(post("/api/rooms")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(rcreq)))
				.andExpect(status().isCreated())
				.andReturn();

		RoomResponseDto room1 = extractData(result1, RoomResponseDto.class);

		// STEP 3 : 사용자 로그인
		log.debug("[STEP 3 : 사용자 로그인]");
		MvcResult user = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req2)))
				.andExpect(status().isOk())
				.andReturn();

		LoginResponse responseUser = extractData(user, LoginResponse.class);
		String accessTokenUser = responseUser.accessToken();

		// STEP 4 : 사용자 스터디룸 조회
		log.debug("[STEP 4 : 스터디룸 조회]");
		MvcResult result2 = mockMvc.perform(get("/api/rooms/" + room1.id())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenUser))
				.andExpect(status().isOk())
				.andReturn();

		RoomResponseDto roomResult = extractData(result2, RoomResponseDto.class);

		// STEP 5 : 해당 room 번호로 예약
		LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0);
		LocalDateTime end = start.plusHours(1);

		ReservationInsertRequest insertReq = new ReservationInsertRequest(
				start.toLocalDate(),
				start,
				end,
				1000L
		);
		
		log.debug("[STEP 5 : 예약]");
		mockMvc.perform(post("/api/reservation/" + roomResult.id())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenUser)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(insertReq)))
				.andExpect(status().isOk());
	}
	
	/*
	 * 1. ADMIN -> create room -> 승인하고 -> 회원 로그인하고 -> 방 목록 조회 -> 예약
	 * 2. ADMIN -> create room -> 승인하고 -> 회원 로그인하고 -> 방 목록 조회 -> 예약
	 *    -. 예약 
	*/
	
	private <T> T extractData(MvcResult result, Class<T> dataClass) throws Exception {
		JavaType jt = objectMapper.getTypeFactory().constructParametricType(ResultDto.class, dataClass);
		
		ResultDto<T> resultDto = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				jt
		);
		
		return resultDto.getData();
	}
	
}
