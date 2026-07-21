package com.mycom.myapp.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.room.dto.RoomCreateRequest;
import com.mycom.myapp.domain.room.dto.RoomResponseDto;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.jwt.JwtProvider;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
	
	/*
	 * ADMIN -> create room -> 승인하고 -> 회원 로그인하고 -> 방 목록 조회 -> 예약
	*/
	@Test
	@Order(1)
	@Transactional
	public void testMVC1() throws Exception {
		
		/*
		INSERT INTO member (username, password, email, name, role, grade, created_at) VALUES ('Admin','password', 'email@test.com', 'name', 'ADMIN', 'BRONZE', CURRENT_TIMESTAMP());
		INSERT INTO member (username,password,email,name,role,grade,created_at) VALUES ('User','password','email@testUser.com','names','USER','BRONZE',CURRENT_TIMESTAMP()); 
		
		SignupRequest signUpAdmin = new SignupRequest("ADMIN", "password", "email@test.com", "admin");
		SignupRequest signUpUser = new SignupRequest("USER", "password", "email@testuser.com", "user");
		
		MvcResult adminAcc = mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpAdmin)))
				.andReturn();
		
		MvcResult userAcc = mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpUser)))
				.andReturn();
		
		MemberResponse response1 = objectMapper.readValue(
		        adminAcc.getResponse().getContentAsString(),
		        MemberResponse.class
		);
		
		MemberResponse response2 = objectMapper.readValue(
		        userAcc.getResponse().getContentAsString(),
		        MemberResponse.class
		);
		*/
		
		// STEP 1 : 시작전 해당 username, password 그리고 ADMIN, USER Role ROW 생성
		LoginRequest req1 = new LoginRequest("Admin", "password");
		LoginRequest req2 = new LoginRequest("User", "password");
		RoomCreateRequest rcreq = new RoomCreateRequest("8조", 3, 10000);
		
		// ADMIN 1 : 관리자 계정으로 로그인
		log.debug("[STEP 1 : ]");
		MvcResult admin = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req1)))
				.andReturn();
		
		LoginResponse responseAdmin = objectMapper.readValue(
		        admin.getResponse().getContentAsString(),
		        LoginResponse.class
		);
		
		// LoginResponse responseAdmin = extractData(admin, new TypeReference<ResultDto<LoginResponse>>() {});

		String accessTokenAdmin = responseAdmin.accessToken();
		
		// ADMIN 2 : 관리자 계정으로 스터디룸 생성
		log.debug("[STEP 2 : ]");
		MvcResult result1 = mockMvc.perform(post("/api/rooms/")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(rcreq)))
				.andReturn();
		
		// USER 1 : 사용자 로그인
		log.debug("[STEP 3 : ]");
		MvcResult user = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req2)))
				.andReturn();
		
		LoginResponse responseUser = objectMapper.readValue(
		        user.getResponse().getContentAsString(),
		        LoginResponse.class
		);

		String accessTokenUser = responseUser.accessToken();
		
		RoomResponseDto room1 = objectMapper.readValue(
		        result1.getResponse().getContentAsString(),
		        RoomResponseDto.class
		);
		
		// USER 2 : 사용자 스터디룸 조회
		log.debug("[STEP 4 : ]");
		MvcResult result2 = mockMvc.perform(get("/api/rooms/" + room1.id())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenUser))
				.andReturn();
		
		// USER 3 : 해당 room 번호로 예약
		RoomResponseDto roomResult = objectMapper.readValue(
		        result2.getResponse().getContentAsString(),
		        RoomResponseDto.class
		);
		
		log.debug("[STEP 5 : ]");
		mockMvc.perform(post("/api/reservation/" + roomResult.id())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenUser))
				.andExpect(status().isOk());
	}
	
	/*
	private extractData() {
		ResultDto<T> resultDto = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				typeRef
		);
	}
	*/
	
}
